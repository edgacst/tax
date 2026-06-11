package com.taxflow.nts.bizverify;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OdcloudNtsBizClient {

    private final RestClient ntsOdcloudRestClient;
    private final NtsBizVerifyProperties properties;

    public JsonNode postStatus(List<String> bizNumbers) {
        return post("/status", Map.of("b_no", bizNumbers));
    }

    public JsonNode postValidate(List<Map<String, String>> businesses) {
        return post("/validate", Map.of("businesses", businesses));
    }

    private JsonNode post(String path, Object body) {
        // 공공데이터포털 인증키는 보통 이미 percent-encoded — 재인코딩 시 키가 깨짐
        URI uri = UriComponentsBuilder.fromUriString(properties.getBaseUrl() + path)
                .queryParam("serviceKey", properties.getServiceKey())
                .build(true)
                .toUri();

        try {
            return ntsOdcloudRestClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "공공데이터포털(국세청) API 오류 HTTP " + e.getStatusCode().value()
                            + (e.getResponseBodyAsString().isBlank() ? "" : ": " + e.getResponseBodyAsString())
            );
        }
    }
}
