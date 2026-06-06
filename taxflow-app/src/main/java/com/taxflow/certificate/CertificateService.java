package com.taxflow.certificate;

import com.taxflow.certificate.api.CertificateDto;
import com.taxflow.tenant.Tenant;
import com.taxflow.tenant.TenantAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final TenantAccess tenantAccess;

    @Transactional(readOnly = true)
    public List<CertificateDto> list(Long tenantIdOrNull) {
        Tenant tenant = tenantAccess.resolve(tenantIdOrNull);
        return tenantAccess.jdbc().query(
                """
                        SELECT id, type, subject_dn, valid_to, status
                        FROM public.certificates
                        WHERE tenant_id = ?
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
