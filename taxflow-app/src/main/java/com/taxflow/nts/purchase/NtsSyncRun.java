package com.taxflow.nts.purchase;

import com.taxflow.tenant.Tenant;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "nts_sync_runs", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NtsSyncRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "sync_type", nullable = false, length = 50)
    @Builder.Default
    private String syncType = "PURCHASE_POLL";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NtsSyncRunStatus status;

    @Column(name = "records_fetched", nullable = false)
    @Builder.Default
    private Integer recordsFetched = 0;

    @Column(name = "records_inserted", nullable = false)
    @Builder.Default
    private Integer recordsInserted = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "started_at", nullable = false)
    @Builder.Default
    private Instant startedAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;
}
