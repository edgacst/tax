package com.taxflow.nts.submit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
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

    @Override
    public NtsSubmissionResult submit(String signedXml, String supplierBizNo) {
        String endpoint = properties.getSoapEndpoint();
        if (endpoint == null || endpoint.isBlank()) {
            return new NtsSubmissionResult("E900", "NTS SOAP endpoint not configured", null, Instant.now());
        }

        String envelope = wrapSoapEnvelope(signedXml);
        try {
            String body = restClientBuilder.build()
                    .post()
                    .uri(endpoint)
                    .contentType(MediaType.TEXT_XML)
                    .accept(MediaType.TEXT_XML)
                    .body(envelope)
                    .retrieve()
                    .body(String.class);

            return parseSoapResponse(body);
        } catch (RestClientResponseException e) {
            log.warn("NTS SOAP HTTP {}: {}", e.getStatusCode().value(), e.getResponseBodyAsString());
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

    private static String wrapSoapEnvelope(String signedXml) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
                  <soapenv:Header/>
                  <soapenv:Body>
                %s
                  </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(signedXml);
    }

    private static NtsSubmissionResult parseSoapResponse(String body) {
        if (body == null || body.isBlank()) {
            return new NtsSubmissionResult("E800", "empty SOAP response", null, Instant.now());
        }
        // 실제 국세청 응답 파서는 테스트베드 연동 시 XSD 기반으로 교체
        String approval = extractBetween(body, "<ApprovalNumber>", "</ApprovalNumber>");
        if (approval == null) {
            approval = extractBetween(body, "<approvalNumber>", "</approvalNumber>");
        }
        if (approval != null && !approval.isBlank()) {
            return new NtsSubmissionResult("0000", "OK", approval.trim(), Instant.now());
        }
        return new NtsSubmissionResult("E801", "approval number not found in response", null, Instant.now());
    }

    private static String extractBetween(String text, String start, String end) {
        int s = text.indexOf(start);
        if (s < 0) {
            return null;
        }
        s += start.length();
        int e = text.indexOf(end, s);
        if (e < 0) {
            return null;
        }
        return text.substring(s, e);
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() > max ? value.substring(0, max) : value;
    }
}
