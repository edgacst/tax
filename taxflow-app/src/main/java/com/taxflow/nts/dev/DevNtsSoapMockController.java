package com.taxflow.nts.dev;

import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * dev 프로필 전용 SOAP 목 서버. {@code NTS_SUBMISSION_SOAP_ENDPOINT=http://127.0.0.1:8080/api/v1/dev/nts/mock/submit}
 */
@Profile("dev")
@RestController
@RequestMapping("/api/v1/dev/nts/mock")
public class DevNtsSoapMockController {

    private static final Pattern BIZ_PATTERN = Pattern.compile("<(?:\\w+:)?SupplierBizNo>(\\d+)</");

    @PostMapping(value = "/submit", consumes = MediaType.TEXT_XML_VALUE, produces = MediaType.TEXT_XML_VALUE)
    public String submit(@RequestBody String body) {
        String biz = "1234567890";
        Matcher m = BIZ_PATTERN.matcher(body != null ? body : "");
        if (m.find()) {
            biz = m.group(1);
        }
        if (body == null || !body.contains("TaxInvoice")) {
            return fault("TaxInvoiceXML missing");
        }
        if (!body.contains("Signature") && !body.contains("ds:Signature")) {
            return fault("XML signature missing");
        }
        String approval = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + biz
                + ThreadLocalRandom.current().nextInt(1000, 9999);
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
                  <soapenv:Body>
                    <SubmitTaxInvoiceResponse>
                      <ResponseCode>0000</ResponseCode>
                      <ResponseMessage>MOCK_OK</ResponseMessage>
                      <ApprovalNumber>%s</ApprovalNumber>
                    </SubmitTaxInvoiceResponse>
                  </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(approval);
    }

    @PostMapping(value = "/purchases", consumes = MediaType.TEXT_XML_VALUE, produces = MediaType.TEXT_XML_VALUE)
    public String purchases(@RequestBody String body) {
        String buyer = "0000000000";
        Matcher m = Pattern.compile("<(?:\\w+:)?BuyerBizNo>(\\d+)</").matcher(body != null ? body : "");
        if (m.find()) {
            buyer = m.group(1);
        }
        String day = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String arn = "MOCK" + day + ThreadLocalRandom.current().nextInt(1000, 9999);
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
                  <soapenv:Body>
                    <ListPurchaseTaxInvoicesResponse>
                      <ResponseCode>0000</ResponseCode>
                      <PurchaseTaxInvoice>
                        <ApprovalNumber>%s</ApprovalNumber>
                        <IssueDate>%s</IssueDate>
                        <SupplierBizNo>9876543210</SupplierBizNo>
                        <SupplierName>목공급자</SupplierName>
                        <BuyerBizNo>%s</BuyerBizNo>
                        <SupplyAmount>100000</SupplyAmount>
                        <TaxAmount>10000</TaxAmount>
                        <TotalAmount>110000</TotalAmount>
                      </PurchaseTaxInvoice>
                    </ListPurchaseTaxInvoicesResponse>
                  </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(arn, day, buyer);
    }

    private static String fault(String message) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
                  <soapenv:Body>
                    <soapenv:Fault>
                      <faultstring>%s</faultstring>
                    </soapenv:Fault>
                  </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(message);
    }
}
