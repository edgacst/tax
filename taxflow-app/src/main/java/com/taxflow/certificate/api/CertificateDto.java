package com.taxflow.certificate.api;

public record CertificateDto(
        Long id,
        String type,
        String subject,
        String validTo,
        String status
) {
}
