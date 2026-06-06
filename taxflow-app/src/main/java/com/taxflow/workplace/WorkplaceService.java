package com.taxflow.workplace;

import com.taxflow.tenant.Tenant;
import com.taxflow.tenant.TenantAccess;
import com.taxflow.workplace.api.WorkplaceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkplaceService {

    private final TenantAccess tenantAccess;

    private static final RowMapper<WorkplaceDto> ROW_MAPPER = (rs, rowNum) -> new WorkplaceDto(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("biz_number"),
            rs.getBoolean("is_default"),
            rs.getString("address")
    );

    @Transactional(readOnly = true)
    public List<WorkplaceDto> list(Long tenantIdOrNull) {
        Tenant tenant = tenantAccess.resolve(tenantIdOrNull);
        String schema = tenantAccess.schemaName(tenant);
        return tenantAccess.jdbc().query(
                """
                        SELECT id, name, biz_number, is_default, address
                        FROM %s.workplaces
                        WHERE status = 'active'
                        ORDER BY is_default DESC, name
                        """.formatted(schema),
                ROW_MAPPER
        );
    }
}
