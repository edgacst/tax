package com.taxflow.auth;

import com.taxflow.audit.AuditLog;
import com.taxflow.audit.AuditService;
import com.taxflow.auth.api.TokenResponseDto;
import com.taxflow.auth.api.UserProfileDto;
import com.taxflow.security.AuthenticatedUser;
import com.taxflow.security.JwtProperties;
import com.taxflow.security.JwtTokenProvider;
import com.taxflow.security.SecurityContextHelper;
import com.taxflow.user.RefreshToken;
import com.taxflow.user.RefreshTokenRepository;
import com.taxflow.user.User;
import com.taxflow.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final AuditService auditService;

    @Transactional
    public TokenResponseDto login(String email, String password, HttpServletRequest request) {
        List<User> candidates = userRepository.findAllByEmailIgnoreCase(email.trim());
        User user = candidates.stream()
                .filter(u -> u.getStatus() == User.UserStatus.ACTIVE)
                .filter(u -> passwordEncoder.matches(password, u.getPasswordHash()))
                .findFirst()
                .orElse(null);

        if (user == null) {
            auditService.log(AuditLog.AUTH_LOGIN_FAILED, "user", null, "{\"email\":\"" + email + "\"}");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        if (user.getTenant() != null && user.getTenant().getStatus() != com.taxflow.tenant.Tenant.TenantStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "테넌트가 활성 상태가 아닙니다.");
        }

        user.setLastLoginAt(Instant.now());
        try {
            if (request.getRemoteAddr() != null) {
                user.setLastLoginIp(InetAddress.getByName(request.getRemoteAddr()));
            }
        } catch (Exception ignored) {
            // best-effort
        }
        userRepository.save(user);

        String access = jwtTokenProvider.createAccessToken(user);
        String refresh = jwtTokenProvider.createRefreshToken(user);
        persistRefreshToken(user.getId(), refresh, request);

        auditService.log(AuditLog.AUTH_LOGIN, "user", user.getId());

        return toTokenResponse(user, access, refresh);
    }

    @Transactional
    public TokenResponseDto refresh(String refreshTokenRaw) {
        Long userId;
        try {
            userId = jwtTokenProvider.parseRefreshUserId(refreshTokenRaw);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 refresh token");
        }

        String hash = hashToken(refreshTokenRaw);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "refresh token not found"));

        if (stored.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(stored);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "refresh token expired");
        }

        User user = userRepository.findByIdWithTenant(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "user not found"));

        refreshTokenRepository.delete(stored);

        String access = jwtTokenProvider.createAccessToken(user);
        String refresh = jwtTokenProvider.createRefreshToken(user);
        persistRefreshToken(user.getId(), refresh, null);

        return toTokenResponse(user, access, refresh);
    }

    @Transactional
    public void logout(String refreshTokenRaw) {
        if (refreshTokenRaw == null || refreshTokenRaw.isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHash(hashToken(refreshTokenRaw))
                .ifPresent(refreshTokenRepository::delete);
        SecurityContextHelper.currentUser().ifPresent(u ->
                auditService.log(AuditLog.AUTH_LOGOUT, "user", u.getUserId()));
    }

    @Transactional(readOnly = true)
    public UserProfileDto me() {
        AuthenticatedUser auth = SecurityContextHelper.requireUser();
        User user = userRepository.findByIdWithTenant(auth.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        return toProfile(user);
    }

    private void persistRefreshToken(Long userId, String refreshTokenRaw, HttpServletRequest request) {
        RefreshToken.RefreshTokenBuilder builder = RefreshToken.builder()
                .userId(userId)
                .tokenHash(hashToken(refreshTokenRaw))
                .expiresAt(jwtTokenProvider.refreshTokenExpiry());
        if (request != null) {
            builder.deviceInfo(truncate(request.getHeader("User-Agent"), 255));
            try {
                if (request.getRemoteAddr() != null) {
                    builder.ipAddress(InetAddress.getByName(request.getRemoteAddr()));
                }
            } catch (Exception ignored) {
                // best-effort
            }
        }
        refreshTokenRepository.save(builder.build());
    }

    private TokenResponseDto toTokenResponse(User user, String access, String refresh) {
        return new TokenResponseDto(
                access,
                refresh,
                jwtProperties.getAccessTokenExpiration() / 1000,
                toProfile(user)
        );
    }

    private UserProfileDto toProfile(User user) {
        return new UserProfileDto(
                user.getId(),
                user.getTenant() != null ? user.getTenant().getId() : null,
                user.getTenant() != null ? user.getTenant().getBusinessName() : null,
                user.getEmail(),
                user.getName(),
                user.getRole().name()
        );
    }

    private static String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() > max ? value.substring(0, max) : value;
    }
}
