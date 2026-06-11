package com.taxflow.nts.soap;

import com.taxflow.nts.submit.NtsSubmissionResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NtsSoapResponseParserTest {

    @Test
    void parsesApprovalNumberAndSuccessCode() {
        String xml = """
                <soapenv:Envelope>
                  <soapenv:Body>
                    <SubmitTaxInvoiceResponse>
                      <ResponseCode>0000</ResponseCode>
                      <ResponseMessage>OK</ResponseMessage>
                      <ApprovalNumber>20260421123456789012</ApprovalNumber>
                    </SubmitTaxInvoiceResponse>
                  </soapenv:Body>
                </soapenv:Envelope>
                """;
        NtsSubmissionResult result = NtsSoapResponseParser.parseSubmissionResponse(xml);
        assertTrue(result.success());
        assertEquals("20260421123456789012", result.approvalNumber());
        assertEquals("0000", result.responseCode());
    }

    @Test
    void parsesFaultAsFailure() {
        String xml = """
                <soapenv:Envelope>
                  <soapenv:Body>
                    <soapenv:Fault>
                      <faultstring>Invalid signature</faultstring>
                    </soapenv:Fault>
                  </soapenv:Body>
                </soapenv:Envelope>
                """;
        NtsSubmissionResult result = NtsSoapResponseParser.parseSubmissionResponse(xml);
        assertFalse(result.success());
        assertTrue(result.responseMessage().contains("Invalid signature"));
    }

    @Test
    void purchaseParserExtractsRows() {
        String xml = """
                <ListPurchaseTaxInvoicesResponse>
                  <PurchaseTaxInvoice>
                    <ApprovalNumber>ARN001</ApprovalNumber>
                    <IssueDate>20260421</IssueDate>
                    <SupplierBizNo>1111111111</SupplierBizNo>
                    <SupplierName>공급자</SupplierName>
                    <BuyerBizNo>2222222222</BuyerBizNo>
                    <SupplyAmount>50000</SupplyAmount>
                    <TaxAmount>5000</TaxAmount>
                    <TotalAmount>55000</TotalAmount>
                  </PurchaseTaxInvoice>
                </ListPurchaseTaxInvoicesResponse>
                """;
        var rows = NtsSoapPurchaseResponseParser.parse(xml, "2222222222");
        assertEquals(1, rows.size());
        assertEquals("ARN001", rows.get(0).ntsApprovalNumber());
        assertEquals(50_000L, rows.get(0).supplyAmount());
    }
}
