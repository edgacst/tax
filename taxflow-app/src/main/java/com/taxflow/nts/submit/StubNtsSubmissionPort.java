package com.taxflow.nts.submit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Primary
@ConditionalOnProperty(name = "taxflow.nts.submission.mode", havingValue = "stub", matchIfMissing = true)
public class StubNtsSubmissionPort implements NtsSubmissionPort {

    @Override
    public NtsSubmissionResult submit(String signedXml, String supplierBizNo) {
        if (signedXml == null || signedXml.isBlank()) {
            return new NtsSubmissionResult("E001", "signed XML is empty", null, Instant.now());
        }
        if (!signedXml.contains("Signature") && !signedXml.contains("ds:Signature")) {
            return new NtsSubmissionResult("E002", "XML signature missing", null, Instant.now());
        }
        String approval = generateApprovalNumber(supplierBizNo);
        return new NtsSubmissionResult("0000", "STUB_ACCEPTED", approval, Instant.now());
    }

    private static String generateApprovalNumber(String bizNo) {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String biz = bizNo != null ? bizNo.replaceAll("\\D", "") : "0000000000";
        if (biz.length() > 10) {
            biz = biz.substring(0, 10);
        }
        int suffix = ThreadLocalRandom.current().nextInt(1000, 9999);
        return date + biz + suffix;
    }
}
