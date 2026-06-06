package com.taxflow.tenant;

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

    public Tenant resolve(Long tenantIdOrNull) {
        if (tenantIdOrNull != null) {
            return tenantRepository.findById(tenantIdOrNull)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "tenant not found"));
        }
        return tenantRepository.findBySchemaName("tenant_demo")
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "tenantId 파라미터를 주거나, tenant_demo 시드가 필요합니다."
                ));
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
