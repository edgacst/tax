package com.taxflow.nts.submit;

import com.taxflow.nts.soap.NtsSoapEnvelopeBuilder;
import com.taxflow.nts.soap.NtsSoapHttpClient;
import com.taxflow.nts.soap.NtsSoapResponseParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "taxflow.nts.submission.mode", havingValue = "soap")
public class SoapNtsSubmissionPort implements NtsSubmissionPort {

    private final NtsSubmissionProperties properties;
    private final RestClient.Builder restClientBuilder;
    private final NtsSoapHttpClient soapHttpClient;

    @Override
    public NtsSubmissionResult submit(String signedXml, String supplierBizNo) {
        String endpoint = properties.getSoapEndpoint();
        if (endpoint == null || endpoint.isBlank()) {
            return new NtsSubmissionResult("E900", "NTS SOAP endpoint not configured", null, Instant.now());
        }
        if (signedXml == null || signedXml.isBlank()) {
            return new NtsSubmissionResult("E001", "signed XML is empty", null, Instant.now());
        }

        String envelope = NtsSoapEnvelopeBuilder.buildSubmitRequest(
                properties.getSoapNamespace(),
                properties.getSoapOperation(),
                supplierBizNo,
                signedXml,
                properties.isSoapUseCdata()
        );

        try {
            String body = soapHttpClient.post(
                    restClientBuilder,
                    endpoint,
                    properties.getSoapAction(),
                    envelope,
                    properties.getConnectTimeoutMs(),
                    properties.getReadTimeoutMs(),
                    properties.getMaxRetries(),
                    properties.getRetryDelayMs()
            );
            return NtsSoapResponseParser.parseSubmissionResponse(body);
        } catch (RestClientResponseException e) {
            log.warn("NTS SOAP HTTP {}: {}", e.getStatusCode().value(), truncate(e.getResponseBodyAsString(), 500));
            if (e.getResponseBodyAsString() != null && !e.getResponseBodyAsString().isBlank()) {
                NtsSubmissionResult parsed = NtsSoapResponseParser.parseSubmissionResponse(e.getResponseBodyAsString());
                if (!parsed.success()) {
                    return new NtsSubmissionResult(
                            "HTTP" + e.getStatusCode().value(),
                            parsed.responseMessage(),
                            null,
                            Instant.now()
                    );
                }
            }
            return new NtsSubmissionResult(
                    "HTTP" + e.getStatusCode().value(),
                    truncate(e.getResponseBodyAsString(), 500),
                    null,
                    Instant.now()
            );
        } catch (Exception e) {
            log.error("NTS SOAP call failed", e);
            return new NtsSubmissionResult("E999", e.getMessage(), null, Instant.now());
        }
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() > max ? value.substring(0, max) : value;
    }
}
