package com.taxflow.nts.submit;

import com.taxflow.audit.AuditLog;
import com.taxflow.audit.AuditService;
import com.taxflow.certificate.TenantCertificateResolver;
import com.taxflow.invoice.api.InvoiceDetailDto;
import com.taxflow.invoice.api.InvoiceItemDto;
import com.taxflow.invoice.api.InvoiceSubmitResponseDto;
import com.taxflow.tenant.Tenant;
import com.taxflow.tenant.TenantAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NtsInvoiceSubmitService {

    private final TenantAccess tenantAccess;
    private final KisaTaxInvoiceXmlGenerator xmlGenerator;
    private final XmlDsigSigner xmlDsigSigner;
    private final TenantCertificateResolver certificateResolver;
    private final NtsSubmissionPort submissionPort;
    private final NtsSubmissionProperties submissionProperties;
    private final AuditService auditService;

    private static final RowMapper<TaxInvoiceSubmitContext> CONTEXT_MAPPER = (rs, rowNum) -> new TaxInvoiceSubmitContext(
            rs.getLong("id"),
            rs.getString("serial_number"),
            rs.getDate("issue_date").toLocalDate(),
            rs.getString("status"),
            rs.getString("direction"),
            rs.getString("remark"),
            new TaxInvoiceSubmitContext.Party(
                    rs.getString("supplier_biz_no"),
                    rs.getString("supplier_name"),
                    rs.getString("supplier_ceo"),
                    rs.getString("supplier_address"),
                    rs.getString("supplier_biz_type"),
                    rs.getString("supplier_biz_item")
            ),
            new TaxInvoiceSubmitContext.Party(
                    rs.getString("buyer_biz_no"),
                    rs.getString("buyer_name"),
                    rs.getString("buyer_ceo"),
                    rs.getString("buyer_address"),
                    "",
                    ""
            ),
            List.of(),
            rs.getLong("total_amount"),
            rs.getLong("total_tax"),
            rs.getLong("total_grand")
    );

    @Transactional
    public InvoiceSubmitResponseDto submit(Long invoiceId) {
        Tenant tenant = tenantAccess.requireTenant();
        String schema = tenantAccess.schemaName(tenant);

        TaxInvoiceSubmitContext base = loadContext(schema, invoiceId);
        if (!"draft".equalsIgnoreCase(base.status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "임시저장(draft) 상태의 세금계산서만 국세청 전송할 수 있습니다.");
        }
        if (!"issue".equalsIgnoreCase(base.direction())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "매출(issue) 세금계산서만 전송할 수 있습니다.");
        }

        List<TaxInvoiceSubmitContext.LineItem> items = loadItems(schema, invoiceId);
        TaxInvoiceSubmitContext ctx = new TaxInvoiceSubmitContext(
                base.invoiceId(), base.serialNo(), base.issueDate(), base.status(), base.direction(), base.remark(),
                base.supplier(), base.buyer(), items,
                base.supplyAmount(), base.taxAmount(), base.grandTotal()
        );

        var signingMaterial = certificateResolver.resolveActive(tenant);
        String xml = xmlGenerator.generate(ctx);
        String signedXml = xmlDsigSigner.signDetached(xml, signingMaterial.privateKey(), signingMaterial.certificate());

        NtsSubmissionResult result = submissionPort.submit(signedXml, ctx.supplier().bizNo());
        Instant now = Instant.now();

        tenantAccess.jdbc().update(
                """
                        INSERT INTO %s.submission_logs (
                          invoice_id, direction, request_xml, response_code, response_message,
                          approval_number, submitted_at, result_received_at
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """.formatted(schema),
                invoiceId,
                ctx.direction(),
                signedXml,
                result.responseCode(),
                result.responseMessage(),
                result.approvalNumber(),
                Timestamp.from(now),
                Timestamp.from(result.receivedAt())
        );

        if (!result.success()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "국세청 전송 실패: " + result.responseCode() + " " + result.responseMessage()
            );
        }

        tenantAccess.jdbc().update(
                """
                        UPDATE %s.tax_invoices
                        SET xml_content = ?, signed_xml = ?, approval_number = ?,
                            status = 'approved', submitted_at = ?, approved_at = ?, updated_at = now()
                        WHERE id = ?
                        """.formatted(schema),
                xml,
                signedXml,
                result.approvalNumber(),
                Timestamp.from(now),
                Timestamp.from(result.receivedAt()),
                invoiceId
        );

        auditService.log(AuditLog.INVOICE_SUBMITTED, "invoice", invoiceId,
                "{\"approvalNumber\":\"" + result.approvalNumber() + "\",\"mode\":\"" + submissionProperties.getMode() + "\"}");

        InvoiceDetailDto detail = loadDetailDto(schema, invoiceId);
        return new InvoiceSubmitResponseDto(
                detail,
                result.approvalNumber(),
                submissionProperties.getMode().name(),
                result.responseMessage()
        );
    }

    private TaxInvoiceSubmitContext loadContext(String schema, Long invoiceId) {
        List<TaxInvoiceSubmitContext> rows = tenantAccess.jdbc().query(
                """
                        SELECT i.id, i.serial_number, i.issue_date, i.status, i.direction, i.remark,
                               i.total_amount, i.total_tax, i.total_grand,
                               w.biz_number AS supplier_biz_no, w.name AS supplier_name,
                               w.ceo_name AS supplier_ceo, w.address AS supplier_address,
                               w.biz_type AS supplier_biz_type, w.biz_item AS supplier_biz_item,
                               p.biz_number AS buyer_biz_no, p.name AS buyer_name,
                               p.ceo_name AS buyer_ceo, p.address AS buyer_address
                        FROM %s.tax_invoices i
                        JOIN %s.workplaces w ON w.id = i.workplace_id
                        JOIN %s.partners p ON p.id = i.partner_id
                        WHERE i.id = ?
                        """.formatted(schema, schema, schema),
                CONTEXT_MAPPER,
                invoiceId
        );
        if (rows.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "invoice not found");
        }
        return rows.get(0);
    }

    private List<TaxInvoiceSubmitContext.LineItem> loadItems(String schema, Long invoiceId) {
        return tenantAccess.jdbc().query(
                """
                        SELECT seq, item_name, quantity, unit_price, amount, tax
                        FROM %s.invoice_items
                        WHERE invoice_id = ?
                        ORDER BY seq
                        """.formatted(schema),
                (rs, rowNum) -> new TaxInvoiceSubmitContext.LineItem(
                        rs.getInt("seq"),
                        rs.getString("item_name"),
                        rs.getDouble("quantity"),
                        rs.getLong("unit_price"),
                        rs.getLong("amount"),
                        rs.getLong("tax")
                ),
                invoiceId
        );
    }

    private InvoiceDetailDto loadDetailDto(String schema, Long invoiceId) {
        var head = tenantAccess.jdbc().queryForMap(
                """
                        SELECT i.id, i.serial_number, i.issue_date, w.name AS workplace_name,
                               p.name AS partner_name, p.biz_number AS partner_biz_no,
                               i.total_amount, i.total_tax, i.total_grand,
                               i.status, i.direction, i.remark, i.approval_number, i.submitted_at
                        FROM %s.tax_invoices i
                        JOIN %s.workplaces w ON w.id = i.workplace_id
                        JOIN %s.partners p ON p.id = i.partner_id
                        WHERE i.id = ?
                        """.formatted(schema, schema, schema),
                invoiceId
        );

        List<InvoiceItemDto> items = tenantAccess.jdbc().query(
                """
                        SELECT id, seq, item_name, spec, quantity, unit_price, amount, tax
                        FROM %s.invoice_items WHERE invoice_id = ? ORDER BY seq
                        """.formatted(schema),
                (rs, rowNum) -> new InvoiceItemDto(
                        rs.getLong("id"), rs.getInt("seq"), rs.getString("item_name"),
                        rs.getString("spec"), rs.getDouble("quantity"),
                        rs.getLong("unit_price"), rs.getLong("amount"), rs.getLong("tax")
                ),
                invoiceId
        );

        Timestamp submitted = (Timestamp) head.get("submitted_at");
        return new InvoiceDetailDto(
                ((Number) head.get("id")).longValue(),
                (String) head.get("serial_number"),
                ((java.sql.Date) head.get("issue_date")).toLocalDate().toString(),
                (String) head.get("workplace_name"),
                (String) head.get("partner_name"),
                (String) head.get("partner_biz_no"),
                ((Number) head.get("total_amount")).longValue(),
                ((Number) head.get("total_tax")).longValue(),
                ((Number) head.get("total_grand")).longValue(),
                (String) head.get("status"),
                (String) head.get("direction"),
                (String) head.get("remark"),
                (String) head.get("approval_number"),
                submitted != null ? submitted.toInstant().toString() : null,
                items
        );
    }
}
