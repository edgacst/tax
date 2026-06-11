package com.taxflow.certificate;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CertificateVaultProperties.class)
public class CertificateConfig {
}
