package com.taxflow.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret = "change-me";
    private long accessTokenExpiration = 900_000L;
    private long refreshTokenExpiration = 604_800_000L;
    private String issuer = "taxflow";
}
