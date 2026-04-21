package com.taxflow.nts.purchase.api;

import com.taxflow.nts.purchase.NtsSyncRunStatus;

import java.time.Instant;

public record SyncRunDto(
        Long id,
        NtsSyncRunStatus status,
        int recordsFetched,
        int recordsInserted,
        String errorMessage,
        Instant startedAt,
        Instant completedAt
) {
}
