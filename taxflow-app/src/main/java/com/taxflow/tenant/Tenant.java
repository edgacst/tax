package com.taxflow.tenant;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "tenants", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String businessName;

    @Column(nullable = false, length = 10, unique = true)
    private String bizNumber;

    @Column(nullable = false, length = 100, unique = true)
    private String schemaName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Plan plan = Plan.FREE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TenantStatus status = TenantStatus.PENDING;

    private String contactEmail;
    private String contactPhone;

    @Column(nullable = false)
    @Builder.Default
    private Integer maxWorkplaces = 1;

    @Column(nullable = false)
    @Builder.Default
    private Integer maxUsers = 3;

    @Column(nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public enum Plan {
        FREE, STANDARD, ENTERPRISE
    }

    public enum TenantStatus {
        PENDING, ACTIVE, SUSPENDED, TERMINATED
    }
}
