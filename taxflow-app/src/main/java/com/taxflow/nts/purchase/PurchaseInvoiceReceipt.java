package com.taxflow.nts.purchase;

import com.fasterxml.jackson.databind.JsonNode;
import com.taxflow.tenant.Tenant;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "purchase_invoice_receipts",
        schema = "public",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "nts_approval_number"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseInvoiceReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "nts_approval_number", nullable = false, length = 30)
    private String ntsApprovalNumber;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "supplier_biz_no", nullable = false, length = 10)
    private String supplierBizNo;

    @Column(name = "supplier_name", nullable = false, length = 200)
    private String supplierName;

    @Column(name = "buyer_biz_no", nullable = false, length = 10)
    private String buyerBizNo;

    @Column(name = "supply_amount", nullable = false)
    private Long supplyAmount;

    @Column(name = "tax_amount", nullable = false)
    private Long taxAmount;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String direction = "receive";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_json", columnDefinition = "jsonb")
    private JsonNode rawJson;

    @Column(name = "synced_at", nullable = false)
    @Builder.Default
    private Instant syncedAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sync_run_id")
    private NtsSyncRun syncRun;
}
