package com.taxflow.certificate;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.cloud.vault.enabled", havingValue = "false", matchIfMissing = true)
public class LocalEncryptedCertificateVault implements CertificateVaultPort {

    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private final CertificateVaultProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public String store(Long tenantId, Long certificateId, byte[] pkcs12Bytes, String certPassword) {
        try {
            Path dir = Path.of(properties.getLocalStorageDir(), String.valueOf(tenantId));
            Files.createDirectories(dir);
            String vaultPath = "local://" + tenantId + "/" + certificateId;
            Path file = dir.resolve(certificateId + ".enc");

            byte[] payload = objectMapper.writeValueAsBytes(Map.of(
                    "pkcs12", Base64.getEncoder().encodeToString(pkcs12Bytes),
                    "password", certPassword
            ));
            byte[] encrypted = encrypt(payload);
            Files.write(file, encrypted);
            return vaultPath;
        } catch (IOException e) {
            throw new IllegalStateException("certificate vault write failed", e);
        }
    }

    @Override
    public StoredCertificate load(String vaultPath) {
        try {
            Path file = resolvePath(vaultPath);
            byte[] encrypted = Files.readAllBytes(file);
            byte[] plain = decrypt(encrypted);
            @SuppressWarnings("unchecked")
            Map<String, String> map = objectMapper.readValue(plain, Map.class);
            return new StoredCertificate(
                    Base64.getDecoder().decode(map.get("pkcs12")),
                    map.get("password")
            );
        } catch (IOException e) {
            throw new IllegalStateException("certificate vault read failed", e);
        }
    }

    @Override
    public void delete(String vaultPath) {
        try {
            Path file = resolvePath(vaultPath);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new IllegalStateException("certificate vault delete failed", e);
        }
    }

    private Path resolvePath(String vaultPath) {
        // local://{tenantId}/{certId}
        String[] parts = vaultPath.replace("local://", "").split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException("invalid vault path: " + vaultPath);
        }
        return Path.of(properties.getLocalStorageDir(), parts[0], parts[1] + ".enc");
    }

    private byte[] encrypt(byte[] plain) throws IOException {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, masterKey(), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] cipherText = cipher.doFinal(plain);
            byte[] out = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(cipherText, 0, out, iv.length, cipherText.length);
            return out;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private byte[] decrypt(byte[] encrypted) throws IOException {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(encrypted, 0, iv, 0, GCM_IV_LENGTH);
            byte[] cipherText = new byte[encrypted.length - GCM_IV_LENGTH];
            System.arraycopy(encrypted, GCM_IV_LENGTH, cipherText, 0, cipherText.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, masterKey(), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            return cipher.doFinal(cipherText);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private SecretKey masterKey() {
        String b64 = properties.getMasterKeyBase64();
        if (b64 == null || b64.isBlank()) {
            throw new IllegalStateException("taxflow.certificate.master-key-base64 is required");
        }
        byte[] key = Base64.getDecoder().decode(b64);
        if (key.length != 32) {
            throw new IllegalStateException("certificate master key must be 32 bytes (Base64)");
        }
        return new SecretKeySpec(key, "AES");
    }
}
