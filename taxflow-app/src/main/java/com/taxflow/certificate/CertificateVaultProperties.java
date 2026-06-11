package com.taxflow.certificate;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "taxflow.certificate")
public class CertificateVaultProperties {

    /**
     * AES-256 master key (Base64, 32 bytes). Dev: application-dev.yml 기본값 사용.
     */
    private String masterKeyBase64 = "";

    /**
     * 로컬 암호화 저장 경로 (Vault 비활성 시).
     */
    private String localStorageDir = "data/certificate-vault";
}
