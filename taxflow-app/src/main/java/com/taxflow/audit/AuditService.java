package com.taxflow.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxflow.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public void log(String action, String resourceType, Long resourceId, String detailJson) {
        try {
            JsonNode detail = parseDetail(detailJson);
            AuditLog entry = AuditLog.builder()
                    .action(action)
                    .resourceType(resourceType)
                    .resourceId(resourceId)
                    .detail(detail)
                    .build();

            fillFromContext(entry);
            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.error("Failed to write audit log: action={}, resource={}", action, resourceType, e);
        }
    }

    public void log(String action, String resourceType, Long resourceId) {
        log(action, resourceType, resourceId, null);
    }

    private JsonNode parseDetail(String detailJson) throws JsonProcessingException {
        if (detailJson == null || detailJson.isBlank()) {
            return null;
        }
        return objectMapper.readTree(detailJson);
    }

    private void fillFromContext(AuditLog entry) {
        try {
            var context = org.springframework.security.core.context.SecurityContextHolder.getContext();
            if (context.getAuthentication() != null
                    && context.getAuthentication().getPrincipal() instanceof AuthenticatedUser authUser) {
                entry.setUserId(authUser.getUserId());
                entry.setTenantId(authUser.getTenantId());
            }
        } catch (Exception ignored) {
            // best-effort
        }

        try {
            var attrs = RequestContextHolder.getRequestAttributes();
            if (attrs instanceof ServletRequestAttributes sra) {
                HttpServletRequest request = sra.getRequest();
                entry.setUserAgent(truncate(request.getHeader("User-Agent"), 500));
                if (request.getRemoteAddr() != null) {
                    entry.setIpAddress(java.net.InetAddress.getByName(request.getRemoteAddr()));
                }
            }
        } catch (Exception ignored) {
            // best-effort
        }
    }

    private String truncate(String value, int maxLen) {
        if (value == null) {
            return null;
        }
        return value.length() > maxLen ? value.substring(0, maxLen) : value;
    }
}
