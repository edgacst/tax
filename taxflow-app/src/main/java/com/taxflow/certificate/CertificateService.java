package com.taxflow.certificate;

import com.taxflow.audit.AuditLog;
import com.taxflow.audit.AuditService;
import com.taxflow.certificate.api.CertificateDto;
import com.taxflow.security.SecurityContextHelper;
import com.taxflow.tenant.Tenant;
import com.taxflow.tenant.TenantAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final TenantAccess tenantAccess;
    private final CertificateVaultPort certificateVault;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<CertificateDto> list() {
        Tenant tenant = tenantAccess.requireTenant();
        return tenantAccess.jdbc().query(
                """
                        SELECT id, type, subject_dn, valid_to, status
                        FROM public.certificates
                        WHERE tenant_id = ? AND status <> 'REVOKED'
                        ORDER BY valid_to DESC
                        """,
                (rs, rowNum) -> {
                    Instant validTo = rs.getTimestamp("valid_to").toInstant();
                    String uiStatus = mapStatus(rs.getString("status"), validTo);
                    return new CertificateDto(
                            rs.getLong("id"),
                            rs.getString("type"),
                            rs.getString("subject_dn"),
                            validTo.atZone(ZoneId.systemDefault()).toLocalDate().toString(),
                            uiStatus
                    );
                },
                tenant.getId()
        );
    }

    @Transactional
    public CertificateDto upload(MultipartFile file, String certPassword) {
        if (!SecurityContextHelper.isTenantAdmin(SecurityContextHelper.requireUser())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "테넌트 관리자만 인증서를 등록할 수 있습니다.");
        }
        Tenant tenant = tenantAccess.requireTenant();
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "인증서 파일이 비어 있습니다.");
        }
        if (certPassword == null || certPassword.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "인증서 비밀번호가 필요합니다.");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "파일을 읽을 수 없습니다.");
        }

        X509Certificate x509 = parsePkcs12(bytes, certPassword);
        String subject = x509.getSubjectX500Principal().getName();
        String issuer = x509.getIssuerX500Principal().getName();
        String serial = x509.getSerialNumber().toString(16);
        Instant validFrom = x509.getNotBefore().toInstant();
        Instant validTo = x509.getNotAfter().toInstant();

        Long userId = SecurityContextHelper.requireUser().getUserId();
        Long certId = tenantAccess.jdbc().queryForObject(
                """
                        INSERT INTO public.certificates (
                            tenant_id, type, subject_dn, issuer_dn, serial_number,
                            valid_from, valid_to, vault_path, status, uploaded_by
                        )
                        VALUES (?, 'PKCS12', ?, ?, ?, ?, ?, 'pending', 'ACTIVE', ?)
                        RETURNING id
                        """,
                Long.class,
                tenant.getId(),
                subject,
                issuer,
                serial,
                java.sql.Timestamp.from(validFrom),
                java.sql.Timestamp.from(validTo),
                userId
        );

        String vaultPath = certificateVault.store(tenant.getId(), certId, bytes, certPassword);
        tenantAccess.jdbc().update(
                "UPDATE public.certificates SET vault_path = ?, updated_at = now() WHERE id = ?",
                vaultPath,
                certId
        );

        auditService.log(AuditLog.CERT_UPLOADED, "certificate", certId);

        return new CertificateDto(
                certId,
                "PKCS12",
                subject,
                validTo.atZone(ZoneId.systemDefault()).toLocalDate().toString(),
                mapStatus("ACTIVE", validTo)
        );
    }

    @Transactional
    public void delete(Long certificateId) {
        if (!SecurityContextHelper.isTenantAdmin(SecurityContextHelper.requireUser())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "테넌트 관리자만 인증서를 삭제할 수 있습니다.");
        }
        Tenant tenant = tenantAccess.requireTenant();
        String vaultPath = tenantAccess.jdbc().query(
                """
                        SELECT vault_path FROM public.certificates
                        WHERE id = ? AND tenant_id = ? AND status <> 'REVOKED'
                        """,
                rs -> rs.next() ? rs.getString("vault_path") : null,
                certificateId,
                tenant.getId()
        );
        if (vaultPath == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "인증서를 찾을 수 없습니다.");
        }

        if (!"pending".equals(vaultPath)) {
            certificateVault.delete(vaultPath);
        }
        tenantAccess.jdbc().update(
                "UPDATE public.certificates SET status = 'REVOKED', updated_at = now() WHERE id = ?",
                certificateId
        );
        auditService.log(AuditLog.CERT_DELETED, "certificate", certificateId);
    }

    private static X509Certificate parsePkcs12(byte[] bytes, String password) {
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(new ByteArrayInputStream(bytes), password.toCharArray());
            String alias = ks.aliases().nextElement();
            return (X509Certificate) ks.getCertificate(alias);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PKCS#12 파일 또는 비밀번호가 올바르지 않습니다.");
        }
    }

    private static String mapStatus(String dbStatus, Instant validTo) {
        if (!"ACTIVE".equalsIgnoreCase(dbStatus)) {
            return "expired";
        }
        long days = ChronoUnit.DAYS.between(Instant.now(), validTo);
        if (days < 0) {
            return "expired";
        }
        if (days <= 30) {
            return "expiring";
        }
        return "active";
    }
}
