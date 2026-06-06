package com.taxflow.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Baseline security; tighten endpoints when auth controllers are wired.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 루트·정적·Swagger·공개 API는 필터 체인을 타지 않게 해 403(권한 없음)이 나지 않도록 함.
     * Actuator 등은 아래 {@link #securityFilterChain(HttpSecurity)} 에서만 제어.
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                .requestMatchers(
                        "/",
                        "/index.html",
                        "/favicon.ico",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/webjars/**",
                        "/error",
                        "/error/**",
                        "/api/v1/public/**",
                        "/api/v1/nts/**",
                        "/api/v1/partners/**",
                        "/api/v1/workplaces/**",
                        "/api/v1/invoices/**",
                        "/api/v1/dashboard/**",
                        "/api/v1/certificates/**"
                )
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}
