package com.taxflow.security;

import com.taxflow.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties properties;

    public String createAccessToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plusMillis(properties.getAccessTokenExpiration());
        return Jwts.builder()
                .issuer(properties.getIssuer())
                .subject(String.valueOf(user.getId()))
                .id(UUID.randomUUID().toString())
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .claim("role", user.getRole().name())
                .claim("tenantId", user.getTenant() != null ? user.getTenant().getId() : null)
                .claim("type", "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(signingKey())
                .compact();
    }

    public String createRefreshToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plusMillis(properties.getRefreshTokenExpiration());
        return Jwts.builder()
                .issuer(properties.getIssuer())
                .subject(String.valueOf(user.getId()))
                .id(UUID.randomUUID().toString())
                .claim("type", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(signingKey())
                .compact();
    }

    public AuthenticatedUser parseAccessToken(String token) {
        Claims claims = parseClaims(token);
        if (!"access".equals(claims.get("type", String.class))) {
            throw new JwtException("not an access token");
        }
        return AuthenticatedUser.builder()
                .userId(Long.parseLong(claims.getSubject()))
                .tenantId(claims.get("tenantId", Long.class))
                .email(claims.get("email", String.class))
                .name(claims.get("name", String.class))
                .role(claims.get("role", String.class))
                .mfaVerified(true)
                .sessionId(claims.getId())
                .build();
    }

    public Long parseRefreshUserId(String token) {
        Claims claims = parseClaims(token);
        if (!"refresh".equals(claims.get("type", String.class))) {
            throw new JwtException("not a refresh token");
        }
        return Long.parseLong(claims.getSubject());
    }

    public Instant refreshTokenExpiry() {
        return Instant.now().plusMillis(properties.getRefreshTokenExpiration());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        byte[] bytes = properties.getSecret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("jwt.secret must be at least 32 bytes");
        }
        return Keys.hmacShaKeyFor(bytes);
    }
}
