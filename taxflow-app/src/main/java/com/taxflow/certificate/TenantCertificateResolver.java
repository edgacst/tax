package com.taxflow.certificate;

import com.taxflow.tenant.Tenant;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TenantCertificateResolver {

    private final com.taxflow.tenant.TenantAccess tenantAccess;
    private final CertificateVaultPort certificateVault;

    public SigningMaterial resolveActive(Tenant tenant) {
        Map<String, Object> row;
        try {
            row = tenantAccess.jdbc().queryForMap(
                    """
                            SELECT id, vault_path, valid_to, status
                            FROM public.certificates
                            WHERE tenant_id = ? AND status = 'ACTIVE'
                              AND vault_path LIKE 'local://%'
                            ORDER BY valid_to DESC
                            LIMIT 1
                            """,
                    tenant.getId()
            );
        } catch (EmptyResultDataAccessException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "사용 가능한 공인인증서가 없습니다. 공인인증서 탭에서 .p12/.pfx 파일을 업로드하세요. (화면에만 보이는 데모 인증서는 실제 파일이 없습니다)"
            );
        }

        Instant validTo = ((java.sql.Timestamp) row.get("valid_to")).toInstant();
        if (validTo.isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "만료된 인증서입니다. 새 인증서를 등록하세요.");
        }

        String vaultPath = (String) row.get("vault_path");
        if (vaultPath == null || vaultPath.isBlank() || "pending".equals(vaultPath)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "인증서 저장소 경로가 유효하지 않습니다.");
        }

        CertificateVaultPort.StoredCertificate stored = certificateVault.load(vaultPath);
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(new ByteArrayInputStream(stored.pkcs12Bytes()), stored.certPassword().toCharArray());
            String alias = ks.aliases().nextElement();
            PrivateKey privateKey = (PrivateKey) ks.getKey(alias, stored.certPassword().toCharArray());
            X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
            return new SigningMaterial(privateKey, cert, ((Number) row.get("id")).longValue());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "인증서를 읽을 수 없습니다.");
        }
    }

    public record SigningMaterial(PrivateKey privateKey, X509Certificate certificate, Long certificateId) {
    }
}
