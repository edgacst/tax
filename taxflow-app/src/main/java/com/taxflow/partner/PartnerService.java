package com.taxflow.partner;

import com.taxflow.audit.AuditLog;
import com.taxflow.audit.AuditService;
import com.taxflow.partner.api.CreatePartnerRequest;
import com.taxflow.partner.api.PartnerDto;
import com.taxflow.tenant.Tenant;
import com.taxflow.tenant.TenantAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PartnerService {

    private final TenantAccess tenantAccess;
    private final AuditService auditService;

    private static final RowMapper<PartnerDto> ROW_MAPPER = (rs, rowNum) -> new PartnerDto(
            rs.getLong("id"),
            rs.getString("biz_number"),
            rs.getString("name"),
            rs.getString("ceo_name"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getBoolean("is_favorite")
    );

    @Transactional(readOnly = true)
    public List<PartnerDto> list(Long tenantIdOrNull, String query) {
        Tenant tenant = tenantAccess.resolve(tenantIdOrNull);
        String schema = tenantAccess.schemaName(tenant);
        String sql = """
                SELECT id, biz_number, name, ceo_name, email, phone, is_favorite
                FROM %s.partners
                WHERE status = 'active'
                """.formatted(schema);
        if (query != null && !query.isBlank()) {
            String like = "%" + query.trim().toLowerCase() + "%";
            return tenantAccess.jdbc().query(
                    sql + """
                             AND (
                               LOWER(name) LIKE ? OR biz_number LIKE ? OR LOWER(COALESCE(ceo_name, '')) LIKE ?
                             )
                             ORDER BY is_favorite DESC, name
                            """,
                    ROW_MAPPER,
                    like, like, like
            );
        }
        return tenantAccess.jdbc().query(sql + " ORDER BY is_favorite DESC, name", ROW_MAPPER);
    }

    @Transactional
    public PartnerDto create(Long tenantIdOrNull, CreatePartnerRequest req) {
        Tenant tenant = tenantAccess.resolve(tenantIdOrNull);
        String schema = tenantAccess.schemaName(tenant);

        Integer dup = tenantAccess.jdbc().queryForObject(
                "SELECT COUNT(*) FROM %s.partners WHERE biz_number = ?".formatted(schema),
                Integer.class,
                req.bizNo()
        );
        if (dup != null && dup > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 등록된 사업자번호입니다.");
        }

        Long id = tenantAccess.jdbc().queryForObject(
                """
                        INSERT INTO %s.partners (biz_number, name, ceo_name, email, phone, is_favorite, status)
                        VALUES (?, ?, ?, ?, ?, ?, 'active')
                        RETURNING id
                        """.formatted(schema),
                Long.class,
                req.bizNo(),
                req.name(),
                req.ceo(),
                req.email(),
                req.phone(),
                req.favorite()
        );
        auditService.log(AuditLog.PARTNER_CREATED, "partner", id);
        return list(tenant.getId(), null).stream()
                .filter(p -> p.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "create failed"));
    }
}
