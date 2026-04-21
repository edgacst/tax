package com.taxflow.nts.purchase;

import java.time.LocalDate;

/**
 * 홈택스(또는 대행) 매입 조회 API에서 내려주는 1건 분량 — 실연동 시 DTO를 XSD/응답 스펙에 맞게 확장.
 */
public record HometaxPurchaseRow(
        String ntsApprovalNumber,
        LocalDate issueDate,
        String supplierBizNo,
        String supplierName,
        String buyerBizNo,
        long supplyAmount,
        long taxAmount,
        long totalAmount,
        String rawJsonPreview
) {
}
