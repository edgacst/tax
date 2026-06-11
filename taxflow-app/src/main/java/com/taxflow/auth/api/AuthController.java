package com.taxflow.auth.api;

import com.taxflow.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "JWT 로그인·토큰 갱신")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "로그인")
    public TokenResponseDto login(@Valid @RequestBody LoginRequest body, HttpServletRequest request) {
        return authService.login(body.email(), body.password(), request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "액세스 토큰 갱신")
    public TokenResponseDto refresh(@Valid @RequestBody RefreshRequest body) {
        return authService.refresh(body.refreshToken());
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃 (refresh token 폐기)")
    public void logout(@RequestBody(required = false) RefreshRequest body) {
        if (body != null) {
            authService.logout(body.refreshToken());
        }
    }

    @GetMapping("/me")
    @Operation(summary = "현재 사용자")
    public UserProfileDto me() {
        return authService.me();
    }
}
