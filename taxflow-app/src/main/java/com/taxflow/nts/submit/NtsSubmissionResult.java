package com.taxflow.nts.submit;

import java.time.Instant;

public record NtsSubmissionResult(
        String responseCode,
        String responseMessage,
        String approvalNumber,
        Instant receivedAt
) {
    public boolean success() {
        return "0000".equals(responseCode) || "OK".equalsIgnoreCase(responseCode);
    }
}
