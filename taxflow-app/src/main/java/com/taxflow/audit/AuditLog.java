package com.taxflow.audit;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.net.InetAddress;
import java.time.Instant;

@Entity
@Table(name = "audit_logs", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(name = "resource_type", length = 50)
    private String resourceType;

    @Column(name = "resource_id")
    private Long resourceId;

    @Column(columnDefinition = "inet")
    private InetAddress ipAddress;

    @Column(columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "request_id", length = 50)
    private String requestId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode detail;

    @Column(nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    public static final String AUTH_LOGIN = "AUTH_LOGIN";
    public static final String AUTH_LOGOUT = "AUTH_LOGOUT";
    public static final String AUTH_LOGIN_FAILED = "AUTH_LOGIN_FAILED";
    public static final String AUTH_MFA_ENABLED = "AUTH_MFA_ENABLED";
    public static final String AUTH_MFA_DISABLED = "AUTH_MFA_DISABLED";

    public static final String TENANT_CREATED = "TENANT_CREATED";
    public static final String TENANT_UPDATED = "TENANT_UPDATED";
    public static final String TENANT_SUSPENDED = "TENANT_SUSPENDED";

    public static final String USER_CREATED = "USER_CREATED";
    public static final String USER_UPDATED = "USER_UPDATED";
    public static final String USER_DELETED = "USER_DELETED";
    public static final String USER_ROLE_CHANGED = "USER_ROLE_CHANGED";

    public static final String PARTNER_CREATED = "PARTNER_CREATED";

    public static final String INVOICE_CREATED = "INVOICE_CREATED";
    public static final String INVOICE_UPDATED = "INVOICE_UPDATED";
    public static final String INVOICE_SUBMITTED = "INVOICE_SUBMITTED";
    public static final String INVOICE_DELETED = "INVOICE_DELETED";

    public static final String CERT_UPLOADED = "CERT_UPLOADED";
    public static final String CERT_UPDATED = "CERT_UPDATED";
    public static final String CERT_DELETED = "CERT_DELETED";

    public static final String DATA_EXPORTED = "DATA_EXPORTED";
}
