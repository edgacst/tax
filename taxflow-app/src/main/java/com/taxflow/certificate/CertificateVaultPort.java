package com.taxflow.certificate;

/**
 * PKCS#12 및 인증서 비밀번호 암호화 저장.
 */
public interface CertificateVaultPort {

    /**
     * @return vault path (DB에 저장)
     */
    String store(Long tenantId, Long certificateId, byte[] pkcs12Bytes, String certPassword);

    StoredCertificate load(String vaultPath);

    void delete(String vaultPath);

    record StoredCertificate(byte[] pkcs12Bytes, String certPassword) {
    }
}
