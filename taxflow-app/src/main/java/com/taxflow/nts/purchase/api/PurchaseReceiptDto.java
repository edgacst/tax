package com.taxflow.nts.purchase.api;

import java.time.Instant;
import java.time.LocalDate;

public record PurchaseReceiptDto(
        Long id,
        String ntsApprovalNumber,
        LocalDate issueDate,
        String supplierBizNo,
        String supplierName,
        String buyerBizNo,
        long supplyAmount,
        long taxAmount,
        long totalAmount,
        Instant syncedAt,
        Long syncRunId
) {
}
