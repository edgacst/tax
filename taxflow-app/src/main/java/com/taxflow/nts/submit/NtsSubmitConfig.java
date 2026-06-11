package com.taxflow.nts.submit;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(NtsSubmissionProperties.class)
public class NtsSubmitConfig {

    @Bean
    public RestClient.Builder ntsRestClientBuilder() {
        return RestClient.builder();
    }
}
