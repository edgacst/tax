package com.taxflow.nts.purchase.api;

public record SyncResponseDto(
        Long syncRunId,
        int recordsFetched,
        int recordsInserted,
        String message
) {
}
