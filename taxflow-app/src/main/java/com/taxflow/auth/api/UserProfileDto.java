package com.taxflow.auth.api;

public record UserProfileDto(
        Long id,
        Long tenantId,
        String tenantName,
        String email,
        String name,
        String role
) {
}
