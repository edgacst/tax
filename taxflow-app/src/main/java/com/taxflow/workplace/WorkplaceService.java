package com.taxflow.workplace;

import com.taxflow.tenant.Tenant;
import com.taxflow.tenant.TenantAccess;
import com.taxflow.workplace.api.SaveWorkplaceRequest;
import com.taxflow.workplace.api.WorkplaceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
            rs.getString("address"),
            rs.getString("ceo_name"),
            rs.getString("biz_type"),
            rs.getString("biz_item"),
            rs.getString("phone"),
            rs.getString("email")
    );

    private static final String SELECT_COLUMNS = """
            id, name, biz_number, is_default, address, ceo_name, biz_type, biz_item, phone, email
            """;

    @Transactional(readOnly = true)
    public List<WorkplaceDto> list(Long tenantIdOrNull) {
        Tenant tenant = tenantAccess.resolve(tenantIdOrNull);
        String schema = tenantAccess.schemaName(tenant);
        return tenantAccess.jdbc().query(
                """
                        SELECT %s
                        FROM %s.workplaces
                        WHERE status = 'active'
                        ORDER BY is_default DESC, name
                        """.formatted(SELECT_COLUMNS, schema),
                ROW_MAPPER
        );
    }

    @Transactional(readOnly = true)
    public WorkplaceDto get(Long tenantIdOrNull, Long id) {
        Tenant tenant = tenantAccess.resolve(tenantIdOrNull);
        String schema = tenantAccess.schemaName(tenant);
        List<WorkplaceDto> rows = tenantAccess.jdbc().query(
                """
                        SELECT %s
                        FROM %s.workplaces
                        WHERE id = ? AND status = 'active'
                        """.formatted(SELECT_COLUMNS, schema),
                ROW_MAPPER,
                id
        );
        if (rows.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사업장을 찾을 수 없습니다.");
        }
        return rows.get(0);
    }

    @Transactional
    public WorkplaceDto create(Long tenantIdOrNull, SaveWorkplaceRequest req) {
        Tenant tenant = tenantAccess.resolve(tenantIdOrNull);
        String schema = tenantAccess.schemaName(tenant);
        assertBizNoUnique(schema, req.bizNo(), null);

        boolean isDefault = req.isDefault() || countActive(schema) == 0;

        Long id = tenantAccess.jdbc().queryForObject(
                """
                        INSERT INTO %s.workplaces (
                            name, biz_number, address, ceo_name, biz_type, biz_item, phone, email, is_default, status
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'active')
                        RETURNING id
                        """.formatted(schema),
                Long.class,
                req.name().trim(),
                req.bizNo(),
                nullToEmpty(req.address()),
                nullToEmpty(req.ceoName()),
                nullToEmpty(req.bizType()),
                nullToEmpty(req.bizItem()),
                nullToEmpty(req.phone()),
                nullToEmpty(req.email()),
                isDefault
        );

        if (isDefault) {
            clearOtherDefaults(schema, id);
        }

        return get(tenant.getId(), id);
    }

    @Transactional
    public WorkplaceDto update(Long tenantIdOrNull, Long id, SaveWorkplaceRequest req) {
        Tenant tenant = tenantAccess.resolve(tenantIdOrNull);
        String schema = tenantAccess.schemaName(tenant);
        get(tenant.getId(), id);
        assertBizNoUnique(schema, req.bizNo(), id);

        int updated = tenantAccess.jdbc().update(
                """
                        UPDATE %s.workplaces
                        SET name = ?, biz_number = ?, address = ?, ceo_name = ?, biz_type = ?,
                            biz_item = ?, phone = ?, email = ?, is_default = ?, updated_at = now()
                        WHERE id = ? AND status = 'active'
                        """.formatted(schema),
                req.name().trim(),
                req.bizNo(),
                nullToEmpty(req.address()),
                nullToEmpty(req.ceoName()),
                nullToEmpty(req.bizType()),
                nullToEmpty(req.bizItem()),
                nullToEmpty(req.phone()),
                nullToEmpty(req.email()),
                req.isDefault(),
                id
        );
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사업장을 찾을 수 없습니다.");
        }

        if (req.isDefault()) {
            clearOtherDefaults(schema, id);
        } else if (!hasDefault(schema)) {
            tenantAccess.jdbc().update(
                    "UPDATE %s.workplaces SET is_default = true, updated_at = now() WHERE id = ?".formatted(schema),
                    id
            );
        }

        return get(tenant.getId(), id);
    }

    private void assertBizNoUnique(String schema, String bizNo, Long excludeId) {
        Integer dup;
        if (excludeId == null) {
            dup = tenantAccess.jdbc().queryForObject(
                    "SELECT COUNT(*) FROM %s.workplaces WHERE biz_number = ? AND status = 'active'".formatted(schema),
                    Integer.class,
                    bizNo
            );
        } else {
            dup = tenantAccess.jdbc().queryForObject(
                    """
                            SELECT COUNT(*) FROM %s.workplaces
                            WHERE biz_number = ? AND status = 'active' AND id <> ?
                            """.formatted(schema),
                    Integer.class,
                    bizNo,
                    excludeId
            );
        }
        if (dup != null && dup > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 등록된 사업자번호입니다.");
        }
    }

    private int countActive(String schema) {
        Integer count = tenantAccess.jdbc().queryForObject(
                "SELECT COUNT(*) FROM %s.workplaces WHERE status = 'active'".formatted(schema),
                Integer.class
        );
        return count == null ? 0 : count;
    }

    private boolean hasDefault(String schema) {
        Integer count = tenantAccess.jdbc().queryForObject(
                "SELECT COUNT(*) FROM %s.workplaces WHERE status = 'active' AND is_default = true".formatted(schema),
                Integer.class
        );
        return count != null && count > 0;
    }

    private void clearOtherDefaults(String schema, Long keepId) {
        tenantAccess.jdbc().update(
                "UPDATE %s.workplaces SET is_default = false, updated_at = now() WHERE id <> ? AND is_default = true"
                        .formatted(schema),
                keepId
        );
        tenantAccess.jdbc().update(
                "UPDATE %s.workplaces SET is_default = true, updated_at = now() WHERE id = ?".formatted(schema),
                keepId
        );
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
