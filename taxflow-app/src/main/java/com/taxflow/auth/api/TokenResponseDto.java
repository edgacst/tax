package com.taxflow.auth.api;

public record TokenResponseDto(
        String accessToken,
        String refreshToken,
        long expiresIn,
        UserProfileDto user
) {
}
