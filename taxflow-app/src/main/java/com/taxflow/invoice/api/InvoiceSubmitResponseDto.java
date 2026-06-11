package com.taxflow.invoice.api;

public record InvoiceSubmitResponseDto(
        InvoiceDetailDto invoice,
        String approvalNumber,
        String submissionMode,
        String message
) {
}
