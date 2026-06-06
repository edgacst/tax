package com.taxflow.nts.bizverify;

import com.fasterxml.jackson.databind.JsonNode;
import com.taxflow.nts.bizverify.api.BizStatusItemDto;
import com.taxflow.nts.bizverify.api.BizStatusResponseDto;
import com.taxflow.nts.bizverify.api.BizValidateItemDto;
import com.taxflow.nts.bizverify.api.BizValidateRequest;
import com.taxflow.nts.bizverify.api.BizValidateResponseDto;
import com.taxflow.nts.bizverify.api.NtsBizVerifyMetaDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NtsBizVerifyService {

    private final NtsBizVerifyProperties properties;
    private final OdcloudNtsBizClient client;

    public NtsBizVerifyMetaDto meta() {
        return new NtsBizVerifyMetaDto(
                properties.isConfigured(),
                properties.isEnabled(),
                properties.getBaseUrl()
        );
    }

    public BizStatusResponseDto queryStatus(List<String> rawBizNumbers) {
        assertConfigured();
        List<String> bizNumbers = normalizeList(rawBizNumbers, 100);
        JsonNode root = client.postStatus(bizNumbers);
        return mapStatusResponse(root);
    }

    public BizValidateResponseDto validate(BizValidateRequest request) {
        assertConfigured();
        Map<String, String> row = new HashMap<>();
        row.put("b_no", normalizeBizNo(request.bizNo()));
        String startDt = digitsOnly(request.startDt());
        if (startDt.length() != 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "개업일자는 YYYYMMDD 8자리여야 합니다.");
        }
        row.put("start_dt", startDt);
        row.put("p_nm", nullToEmpty(request.ceoName()));
        row.put("p_nm2", nullToEmpty(request.ceoName2()));
        row.put("b_nm", nullToEmpty(request.corpName()));
        row.put("corp_no", digitsOnly(nullToEmpty(request.corpNo())));
        row.put("b_sector", nullToEmpty(request.bizSector()));
        row.put("b_type", nullToEmpty(request.bizType()));
        row.put("b_adr", nullToEmpty(request.address()));

        JsonNode root = client.postValidate(List.of(row));
        return mapValidateResponse(root);
    }

    private void assertConfigured() {
        if (!properties.isConfigured()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "국세청 사업자 조회가 비활성화되었습니다. taxflow.nts.biz-verify.enabled=true 와 "
                            + "NTS_BIZ_VERIFY_SERVICE_KEY(공공데이터포털 인증키)를 설정하세요."
            );
        }
    }

    private static List<String> normalizeList(List<String> raw, int max) {
        if (raw == null || raw.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사업자등록번호가 필요합니다.");
        }
        List<String> out = new ArrayList<>();
        for (String s : raw) {
            if (s == null || s.isBlank()) {
                continue;
            }
            out.add(normalizeBizNo(s));
            if (out.size() >= max) {
                break;
            }
        }
        if (out.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효한 사업자등록번호가 없습니다.");
        }
        return out;
    }

    private static String normalizeBizNo(String bizNo) {
        String digits = digitsOnly(bizNo);
        if (digits.length() != 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사업자등록번호는 10자리 숫자여야 합니다.");
        }
        return digits;
    }

    private static String digitsOnly(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("\\D", "");
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static BizStatusResponseDto mapStatusResponse(JsonNode root) {
        String statusCode = text(root, "status_code");
        List<BizStatusItemDto> items = new ArrayList<>();
        JsonNode data = root.path("data");
        if (data.isArray()) {
            for (JsonNode node : data) {
                String taxType = text(node, "tax_type");
                items.add(new BizStatusItemDto(
                        text(node, "b_no"),
                        text(node, "b_stt"),
                        text(node, "b_stt_cd"),
                        taxType,
                        text(node, "tax_type_cd"),
                        text(node, "end_dt"),
                        text(node, "utcc_yn"),
                        isRegistered(taxType)
                ));
            }
        }
        return new BizStatusResponseDto(statusCode, items);
    }

    private static BizValidateResponseDto mapValidateResponse(JsonNode root) {
        String statusCode = text(root, "status_code");
        List<BizValidateItemDto> items = new ArrayList<>();
        JsonNode data = root.path("data");
        if (data.isArray()) {
            for (JsonNode node : data) {
                String validCode = text(node, "valid");
                items.add(new BizValidateItemDto(
                        text(node, "b_no"),
                        "01".equals(validCode),
                        validCode,
                        text(node, "valid_msg")
                ));
            }
        }
        return new BizValidateResponseDto(statusCode, items);
    }

    private static boolean isRegistered(String taxType) {
        if (taxType == null || taxType.isBlank()) {
            return false;
        }
        return !taxType.contains("등록되지 않은");
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.path(field);
        return v.isMissingNode() || v.isNull() ? "" : v.asText("");
    }
}
