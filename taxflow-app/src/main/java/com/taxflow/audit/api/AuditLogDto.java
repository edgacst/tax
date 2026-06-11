package com.taxflow.audit.api;

import java.time.Instant;

public record AuditLogDto(
        Long id,
        String action,
        String resourceType,
        Long resourceId,
        Instant createdAt
) {
}
