package com.taxflow.dashboard;

import com.taxflow.dashboard.api.DashboardSummaryDto;
import com.taxflow.dashboard.api.InvoiceSummaryDto;
import com.taxflow.tenant.Tenant;
import com.taxflow.tenant.TenantAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TenantAccess tenantAccess;

    @Transactional(readOnly = true)
    public DashboardSummaryDto summary(Long tenantIdOrNull) {
        Tenant tenant = tenantAccess.resolve(tenantIdOrNull);
        String schema = tenantAccess.schemaName(tenant);

        int approved = count(schema, "status = 'approved'");
        int draft = count(schema, "status = 'draft'");
        Long salesTotal = tenantAccess.jdbc().queryForObject(
                """
                        SELECT COALESCE(SUM(total_grand), 0)
                        FROM %s.tax_invoices
                        WHERE direction = 'issue' AND status = 'approved'
                        """.formatted(schema),
                Long.class
        );
        int partners = tenantAccess.jdbc().queryForObject(
                "SELECT COUNT(*) FROM %s.partners WHERE status = 'active'".formatted(schema),
                Integer.class
        );

        List<InvoiceSummaryDto> recent = tenantAccess.jdbc().query(
                """
                        SELECT i.id, i.serial_number, i.issue_date, p.name AS partner_name,
                               i.total_grand, i.status, i.direction
                        FROM %s.tax_invoices i
                        JOIN %s.partners p ON p.id = i.partner_id
                        ORDER BY i.issue_date DESC, i.id DESC
                        LIMIT 5
                        """.formatted(schema, schema),
                (rs, rowNum) -> new InvoiceSummaryDto(
                        rs.getLong("id"),
                        rs.getString("serial_number"),
                        rs.getDate("issue_date").toLocalDate().toString(),
                        rs.getString("partner_name"),
                        rs.getLong("total_grand"),
                        rs.getString("status"),
                        rs.getString("direction")
                )
        );

        return new DashboardSummaryDto(
                approved,
                draft,
                salesTotal != null ? salesTotal : 0L,
                partners,
                recent
        );
    }

    private int count(String schema, String where) {
        Integer n = tenantAccess.jdbc().queryForObject(
                "SELECT COUNT(*) FROM %s.tax_invoices WHERE %s".formatted(schema, where),
                Integer.class
        );
        return n != null ? n : 0;
    }
}
