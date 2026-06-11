package com.taxflow.nts.soap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.function.Supplier;

@Slf4j
@Component
public class NtsSoapHttpClient {

    public String post(
            RestClient.Builder restClientBuilder,
            String endpoint,
            String soapAction,
            String envelope,
            int connectTimeoutMs,
            int readTimeoutMs,
            int maxRetries,
            long retryDelayMs
    ) {
        var requestFactory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        RestClient client = restClientBuilder.requestFactory(requestFactory).build();

        int attempts = Math.max(1, maxRetries + 1);
        RuntimeException last = null;

        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                var spec = client.post()
                        .uri(endpoint)
                        .contentType(MediaType.parseMediaType("text/xml; charset=UTF-8"))
                        .accept(MediaType.TEXT_XML, MediaType.APPLICATION_XML)
                        .body(envelope);
                if (soapAction != null && !soapAction.isBlank()) {
                    spec = spec.header("SOAPAction", quoteSoapAction(soapAction));
                }
                return spec.retrieve().body(String.class);
            } catch (RestClientResponseException e) {
                last = e;
                if (!isRetryable(e.getStatusCode().value()) || attempt >= attempts) {
                    throw e;
                }
                log.warn("NTS SOAP HTTP {} attempt {}/{}, retrying...", e.getStatusCode().value(), attempt, attempts);
                sleep(retryDelayMs * attempt);
            } catch (RuntimeException e) {
                last = e;
                if (attempt >= attempts) {
                    throw e;
                }
                log.warn("NTS SOAP call failed attempt {}/{}: {}", attempt, attempts, e.getMessage());
                sleep(retryDelayMs * attempt);
            }
        }
        throw last != null ? last : new IllegalStateException("SOAP call failed");
    }

    public static <T> T withRetry(Supplier<T> action, int maxRetries, long retryDelayMs) {
        int attempts = Math.max(1, maxRetries + 1);
        RuntimeException last = null;
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                return action.get();
            } catch (RuntimeException e) {
                last = e;
                if (attempt >= attempts) {
                    throw e;
                }
                sleep(retryDelayMs * attempt);
            }
        }
        throw last != null ? last : new IllegalStateException("retry exhausted");
    }

    private static boolean isRetryable(int status) {
        return status == 408 || status == 429 || status >= 500;
    }

    private static String quoteSoapAction(String action) {
        String trimmed = action.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            return trimmed;
        }
        return "\"" + trimmed + "\"";
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(Math.max(0, ms));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted during SOAP retry", e);
        }
    }
}
