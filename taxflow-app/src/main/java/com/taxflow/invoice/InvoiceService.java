package com.taxflow.invoice;

import com.taxflow.audit.AuditLog;
import com.taxflow.audit.AuditService;
import com.taxflow.invoice.api.*;
import com.taxflow.tenant.Tenant;
import com.taxflow.tenant.TenantAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final TenantAccess tenantAccess;
    private final AuditService auditService;

    private static final RowMapper<InvoiceDto> LIST_MAPPER = (rs, rowNum) -> new InvoiceDto(
            rs.getLong("id"),
            rs.getString("serial_number"),
            rs.getDate("issue_date").toLocalDate().toString(),
            rs.getString("workplace_name"),
            rs.getString("partner_name"),
            rs.getString("partner_biz_no"),
            rs.getLong("total_amount"),
            rs.getLong("total_tax"),
            rs.getLong("total_grand"),
            rs.getString("status"),
            rs.getString("direction"),
            rs.getString("remark")
    );

    @Transactional(readOnly = true)
    public List<InvoiceDto> list(Long tenantIdOrNull, String query) {
        Tenant tenant = tenantAccess.resolve(tenantIdOrNull);
        String schema = tenantAccess.schemaName(tenant);
        String base = """
                SELECT i.id, i.serial_number, i.issue_date, w.name AS workplace_name,
                       p.name AS partner_name, p.biz_number AS partner_biz_no,
                       i.total_amount, i.total_tax, i.total_grand,
                       i.status, i.direction, i.remark
                FROM %s.tax_invoices i
                JOIN %s.workplaces w ON w.id = i.workplace_id
                JOIN %s.partners p ON p.id = i.partner_id
                """.formatted(schema, schema, schema);
        if (query != null && !query.isBlank()) {
            String like = "%" + query.trim().toLowerCase() + "%";
            return tenantAccess.jdbc().query(
                    base + """
                            WHERE LOWER(p.name) LIKE ? OR LOWER(i.serial_number) LIKE ? OR p.biz_number LIKE ?
                            ORDER BY i.issue_date DESC, i.id DESC
                            """,
                    LIST_MAPPER,
                    like, like, like
            );
        }
        return tenantAccess.jdbc().query(
                base + " ORDER BY i.issue_date DESC, i.id DESC",
                LIST_MAPPER
        );
    }

    @Transactional(readOnly = true)
    public InvoiceDetailDto getById(Long tenantIdOrNull, Long invoiceId) {
        Tenant tenant = tenantAccess.resolve(tenantIdOrNull);
        String schema = tenantAccess.schemaName(tenant);
        List<InvoiceDto> rows = tenantAccess.jdbc().query(
                """
                        SELECT i.id, i.serial_number, i.issue_date, w.name AS workplace_name,
                               p.name AS partner_name, p.biz_number AS partner_biz_no,
                               i.total_amount, i.total_tax, i.total_grand,
                               i.status, i.direction, i.remark
                        FROM %s.tax_invoices i
                        JOIN %s.workplaces w ON w.id = i.workplace_id
                        JOIN %s.partners p ON p.id = i.partner_id
                        WHERE i.id = ?
                        """.formatted(schema, schema, schema),
                LIST_MAPPER,
                invoiceId
        );
        if (rows.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "invoice not found");
        }
        InvoiceDto head = rows.get(0);
        List<InvoiceItemDto> items = tenantAccess.jdbc().query(
                """
                        SELECT id, seq, item_name, spec, quantity, unit_price, amount, tax
                        FROM %s.invoice_items
                        WHERE invoice_id = ?
                        ORDER BY seq
                        """.formatted(schema),
                (rs, rowNum) -> new InvoiceItemDto(
                        rs.getLong("id"),
                        rs.getInt("seq"),
                        rs.getString("item_name"),
                        rs.getString("spec"),
                        rs.getDouble("quantity"),
                        rs.getLong("unit_price"),
                        rs.getLong("amount"),
                        rs.getLong("tax")
                ),
                invoiceId
        );
        var meta = tenantAccess.jdbc().query(
                """
                        SELECT approval_number, submitted_at
                        FROM %s.tax_invoices WHERE id = ?
                        """.formatted(schema),
                (rs, rowNum) -> new Object[] {
                        rs.getString("approval_number"),
                        rs.getTimestamp("submitted_at")
                },
                invoiceId
        );
        String approvalNumber = null;
        String submittedAt = null;
        if (!meta.isEmpty()) {
            approvalNumber = (String) meta.get(0)[0];
            java.sql.Timestamp ts = (java.sql.Timestamp) meta.get(0)[1];
            submittedAt = ts != null ? ts.toInstant().toString() : null;
        }

        return new InvoiceDetailDto(
                head.id(), head.serialNo(), head.issueDate(), head.workplaceName(),
                head.partnerName(), head.partnerBizNo(), head.supplyAmount(), head.tax(),
                head.total(), head.status(), head.direction(), head.remark(),
                approvalNumber, submittedAt, items
        );
    }

    @Transactional
    public InvoiceDetailDto createDraft(Long tenantIdOrNull, CreateInvoiceRequest req) {
        Tenant tenant = tenantAccess.resolve(tenantIdOrNull);
        String schema = tenantAccess.schemaName(tenant);

        assertExists(schema, "workplaces", req.workplaceId());
        assertExists(schema, "partners", req.partnerId());

        long supply = 0;
        long tax = 0;
        for (CreateInvoiceItemRequest item : req.items()) {
            long amount = Math.round(item.quantity() * item.unitPrice());
            long itemTax = Math.floorDiv(amount * 10, 100);
            supply += amount;
            tax += itemTax;
        }
        long grand = supply + tax;
        String direction = req.direction() != null && !req.direction().isBlank() ? req.direction() : "issue";
        String serial = nextSerial(schema, req.workplaceId(), req.issueDate(), direction);

        Long invoiceId = tenantAccess.jdbc().queryForObject(
                """
                        INSERT INTO %s.tax_invoices (
                          workplace_id, partner_id, issue_date, serial_number,
                          total_amount, total_tax, total_grand, remark, status, direction
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'draft', ?)
                        RETURNING id
                        """.formatted(schema),
                Long.class,
                req.workplaceId(),
                req.partnerId(),
                req.issueDate(),
                serial,
                supply,
                tax,
                grand,
                req.remark(),
                direction
        );

        int seq = 1;
        for (CreateInvoiceItemRequest item : req.items()) {
            long amount = Math.round(item.quantity() * item.unitPrice());
            long itemTax = Math.floorDiv(amount * 10, 100);
            tenantAccess.jdbc().update(
                    """
                            INSERT INTO %s.invoice_items (
                              invoice_id, seq, item_name, quantity, unit_price, amount, tax_rate, tax
                            ) VALUES (?, ?, ?, ?, ?, ?, 10.00, ?)
                            """.formatted(schema),
                    invoiceId,
                    seq++,
                    item.itemName(),
                    item.quantity(),
                    item.unitPrice(),
                    amount,
                    itemTax
            );
        }
        auditService.log(AuditLog.INVOICE_CREATED, "invoice", invoiceId);
        return getById(null, invoiceId);
    }

    private void assertExists(String schema, String table, Long id) {
        Integer count = tenantAccess.jdbc().queryForObject(
                "SELECT COUNT(*) FROM %s.%s WHERE id = ?".formatted(schema, table),
                Integer.class,
                id
        );
        if (count == null || count == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, table + " not found: " + id);
        }
    }

    private String nextSerial(String schema, Long workplaceId, LocalDate issueDate, String direction) {
        String workplaceName = tenantAccess.jdbc().queryForObject(
                "SELECT name FROM %s.workplaces WHERE id = ?".formatted(schema),
                String.class,
                workplaceId
        );
        String code = workplaceCode(workplaceName);
        int year = issueDate.getYear();
        String prefix = year + "-" + code;

        Integer last = tenantAccess.jdbc().queryForObject(
                """
                        INSERT INTO %s.invoice_serials (workplace_id, year, direction, last_number, prefix)
                        VALUES (?, ?, ?, 1, ?)
                        ON CONFLICT (workplace_id, year, direction)
                        DO UPDATE SET last_number = %s.invoice_serials.last_number + 1, updated_at = now()
                        RETURNING last_number
                        """.formatted(schema, schema),
                Integer.class,
                workplaceId,
                year,
                direction,
                prefix
        );
        int n = last != null ? last : 1;
        return "%s-%04d".formatted(prefix, n);
    }

    private static String workplaceCode(String name) {
        if (name == null) {
            return "MAIN";
        }
        return switch (name) {
            case "본사" -> "SEOUL";
            case "부산지점" -> "BUSAN";
            default -> {
                String s = name.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
                yield s.isEmpty() ? "MAIN" : s.substring(0, Math.min(6, s.length()));
            }
        };
    }
}
