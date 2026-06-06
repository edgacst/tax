package com.taxflow.dashboard.api;

public record InvoiceSummaryDto(
        Long id,
        String serialNo,
        String issueDate,
        String partnerName,
        long total,
        String status,
        String direction
) {
}
