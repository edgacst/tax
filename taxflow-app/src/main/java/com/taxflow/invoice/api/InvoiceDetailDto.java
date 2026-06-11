package com.taxflow.invoice.api;

import java.util.List;

public record InvoiceDetailDto(
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
        String remark,
        String approvalNumber,
        String submittedAt,
        List<InvoiceItemDto> items
) {
}
