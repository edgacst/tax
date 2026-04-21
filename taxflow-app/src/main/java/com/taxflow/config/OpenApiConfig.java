package com.taxflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI taxflowOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("TaxFlow API")
                        .description("전자세금계산서 SaaS 백엔드 (로컬 개발용 스켈레톤)")
                        .version("0.1.0"));
    }
}
