package com.taxflow.nts.soap;

import com.taxflow.nts.submit.NtsSubmissionResult;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

/**
 * 국세청 SOAP 응답 파싱 (태그명 변형 허용).
 */
public final class NtsSoapResponseParser {

    private static final List<String> APPROVAL_TAGS = List.of(
            "ApprovalNumber", "approvalNumber", "IssueConfirmNum", "issueConfirmNum",
            "NTSSendKey", "ntsSendKey", "RefSubmitID"
    );

    private static final List<String> CODE_TAGS = List.of(
            "ResponseCode", "responseCode", "ResultCode", "resultCode", "StatusCode", "statusCode"
    );

    private static final List<String> MESSAGE_TAGS = List.of(
            "ResponseMessage", "responseMessage", "ResultMessage", "resultMessage",
            "ErrorMessage", "errorMessage", "StatusMessage", "statusMessage"
    );

    private NtsSoapResponseParser() {
    }

    public static NtsSubmissionResult parseSubmissionResponse(String body) {
        if (body == null || body.isBlank()) {
            return new NtsSubmissionResult("E800", "empty SOAP response", null, Instant.now());
        }

        String code = firstTagValue(body, CODE_TAGS);
        String message = firstTagValue(body, MESSAGE_TAGS);
        String approval = firstTagValue(body, APPROVAL_TAGS);

        if (code == null && approval != null) {
            code = "0000";
        }
        if (code == null && isFault(body)) {
            code = "SOAP_FAULT";
            message = extractFaultString(body);
        }
        if (code == null && approval != null) {
            return new NtsSubmissionResult("0000", nullToDefault(message, "OK"), approval.trim(), Instant.now());
        }
        if (code != null && isSuccessCode(code) && approval != null && !approval.isBlank()) {
            return new NtsSubmissionResult(normalizeCode(code), nullToDefault(message, "OK"), approval.trim(), Instant.now());
        }
        if (code != null && isSuccessCode(code) && (approval == null || approval.isBlank())) {
            return new NtsSubmissionResult(normalizeCode(code), nullToDefault(message, "OK without approval number"), null, Instant.now());
        }
        if (approval != null && !approval.isBlank()) {
            return new NtsSubmissionResult("0000", nullToDefault(message, "OK"), approval.trim(), Instant.now());
        }

        String errCode = code != null ? normalizeCode(code) : "E801";
        String errMsg = message != null ? message : "approval number not found in SOAP response";
        return new NtsSubmissionResult(errCode, errMsg, null, Instant.now());
    }

    public static boolean isFault(String body) {
        return body.contains(":Fault>") || body.contains("<Fault>");
    }

    private static String extractFaultString(String body) {
        String fault = firstTagValue(body, List.of("faultstring", "Faultstring", "faultString"));
        if (fault != null) {
            return fault;
        }
        return truncate(body, 300);
    }

    public static String firstTagValue(String xml, List<String> tagNames) {
        for (String tag : tagNames) {
            String value = extractBetween(xml, "<" + tag + ">", "</" + tag + ">");
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
            String prefixed = extractBetween(xml, ":" + tag + ">", "</");
            if (prefixed != null) {
                int end = prefixed.indexOf('<');
                if (end > 0) {
                    String v = prefixed.substring(0, end).trim();
                    if (!v.isBlank()) {
                        return v;
                    }
                }
            }
        }
        return null;
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

    private static boolean isSuccessCode(String code) {
        String c = code.trim().toUpperCase(Locale.ROOT);
        return "0000".equals(c) || "00".equals(c) || "OK".equals(c) || "SUCCESS".equals(c) || "0".equals(c);
    }

    private static String normalizeCode(String code) {
        if (isSuccessCode(code)) {
            return "0000";
        }
        return code.trim();
    }

    private static String nullToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        String compact = value.replaceAll("\\s+", " ").trim();
        return compact.length() > max ? compact.substring(0, max) : compact;
    }
}
