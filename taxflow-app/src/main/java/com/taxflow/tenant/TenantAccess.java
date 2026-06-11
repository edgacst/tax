package com.taxflow.tenant;

import com.taxflow.security.AuthenticatedUser;
import com.taxflow.security.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class TenantAccess {

    private static final Pattern SCHEMA_PATTERN = Pattern.compile("^[a-z][a-z0-9_]*$");

    private final TenantRepository tenantRepository;
    private final JdbcTemplate jdbcTemplate;

    /**
     * JWT에 담긴 tenantId로 테넌트를 조회합니다. SUPER_ADMIN만 tenantId 파라미터로 다른 테넌트 접근 가능.
     */
    public Tenant requireTenant(Long tenantIdOverride) {
        AuthenticatedUser user = SecurityContextHelper.requireUser();
        Long tenantId = user.getTenantId();
        if (tenantIdOverride != null) {
            if (!tenantIdOverride.equals(tenantId) && !"SUPER_ADMIN".equals(user.getRole())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "다른 테넌트 데이터에 접근할 수 없습니다.");
            }
            tenantId = tenantIdOverride;
        }
        if (tenantId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "테넌트가 할당되지 않은 사용자입니다.");
        }
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "tenant not found"));
    }

    public Tenant requireTenant() {
        return requireTenant(null);
    }

    @Deprecated
    public Tenant resolve(Long tenantIdOrNull) {
        return requireTenant(tenantIdOrNull);
    }

    public String schemaName(Tenant tenant) {
        String schema = tenant.getSchemaName();
        if (!SCHEMA_PATTERN.matcher(schema).matches()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "invalid tenant schema");
        }
        return schema;
    }

    public JdbcTemplate jdbc() {
        return jdbcTemplate;
    }
}
