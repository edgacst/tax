package com.taxflow.invoice.api;

public record InvoiceDto(
        Long id,
        String serialNo,
        String issueDate,
        String workplaceName,
        String partnerName,
        String partnerBizNo,
        long supplyAmount,
        long tax,
        long total,
        String status,
        String direction,
        String remark
) {
}
