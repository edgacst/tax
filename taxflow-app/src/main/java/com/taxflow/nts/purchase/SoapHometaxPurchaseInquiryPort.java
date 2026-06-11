package com.taxflow.nts.purchase;

import com.taxflow.nts.soap.NtsSoapEnvelopeBuilder;
import com.taxflow.nts.soap.NtsSoapHttpClient;
import com.taxflow.nts.soap.NtsSoapPurchaseResponseParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "taxflow.nts.purchase.mode", havingValue = "soap")
public class SoapHometaxPurchaseInquiryPort implements HometaxPurchaseInquiryPort {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final NtsPurchaseProperties properties;
    private final RestClient.Builder restClientBuilder;
    private final NtsSoapHttpClient soapHttpClient;

    @Override
    public List<HometaxPurchaseRow> fetchPurchaseInvoicesSince(
            Long tenantId,
            Instant sinceExclusive,
            String tenantBizNo
    ) {
        String endpoint = properties.getSoapEndpoint();
        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalStateException("NTS purchase SOAP endpoint not configured");
        }

        String sinceDate = sinceExclusive.atZone(KST).toLocalDate().format(DateTimeFormatter.BASIC_ISO_DATE);
        String envelope = NtsSoapEnvelopeBuilder.buildListPurchaseRequest(
                properties.getSoapNamespace(),
                properties.getSoapListOperation(),
                tenantBizNo,
                sinceDate
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
            return NtsSoapPurchaseResponseParser.parse(body, tenantBizNo);
        } catch (RestClientResponseException e) {
            log.warn("NTS purchase SOAP HTTP {}: {}", e.getStatusCode().value(), e.getResponseBodyAsString());
            throw new IllegalStateException("매입 조회 SOAP 실패: HTTP " + e.getStatusCode().value(), e);
        }
    }
}
