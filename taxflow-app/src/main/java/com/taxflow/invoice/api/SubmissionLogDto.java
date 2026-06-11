package com.taxflow.invoice.api;

public record SubmissionLogDto(
        Long id,
        String responseCode,
        String responseMessage,
        String approvalNumber,
        String submittedAt,
        boolean success
) {
}
