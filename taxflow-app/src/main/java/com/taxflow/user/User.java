package com.taxflow.user;

import com.taxflow.tenant.Tenant;
import jakarta.persistence.*;
import lombok.*;

import java.net.InetAddress;
import java.time.Instant;

@Entity
@Table(name = "users", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String name;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private Role role = Role.VIEWER;

    @Column(name = "mfa_secret", length = 255)
    private String mfaSecret;

    @Column(name = "mfa_enabled", nullable = false)
    @Builder.Default
    private Boolean mfaEnabled = false;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "last_login_ip", columnDefinition = "inet")
    private InetAddress lastLoginIp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

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

    public boolean isSuperAdmin() {
        return this.role == Role.SUPER_ADMIN;
    }

    public boolean belongsToTenant(Long tenantId) {
        return tenant != null && tenant.getId().equals(tenantId);
    }

    public enum Role {
        SUPER_ADMIN, TENANT_ADMIN, MANAGER, VIEWER
    }

    public enum UserStatus {
        ACTIVE, INACTIVE, LOCKED, PENDING_VERIFICATION
    }
}
