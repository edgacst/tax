package com.taxflow.audit.api;

import com.taxflow.audit.AuditLogRepository;
import com.taxflow.security.SecurityContextHelper;
import com.taxflow.tenant.TenantAccess;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "감사 로그")
public class AuditController {

    private final AuditLogRepository auditLogRepository;
    private final TenantAccess tenantAccess;

    @GetMapping
    @Operation(summary = "테넌트 감사 로그 (최근)")
    public List<AuditLogDto> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        if (!SecurityContextHelper.isTenantAdmin(SecurityContextHelper.requireUser())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 조회할 수 있습니다.");
        }
        Long tenantId = tenantAccess.requireTenant().getId();
        int safeSize = Math.min(Math.max(size, 1), 100);
        return auditLogRepository
                .findByTenantIdOrderByCreatedAtDesc(tenantId, PageRequest.of(page, safeSize))
                .stream()
                .map(e -> new AuditLogDto(
                        e.getId(),
                        e.getAction(),
                        e.getResourceType(),
                        e.getResourceId(),
                        e.getCreatedAt()
                ))
                .toList();
    }
}
