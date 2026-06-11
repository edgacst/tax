package com.taxflow.nts.soap;

/**
 * 국세청 연계용 SOAP 1.1 Envelope 생성.
 * <p>KEC 표준전자세금계산서 v3.0 — 요청 본문에 서명된 XML을 CDATA로 포함.</p>
 */
public final class NtsSoapEnvelopeBuilder {

    private NtsSoapEnvelopeBuilder() {
    }

    public static String buildSubmitRequest(
            String namespace,
            String operationElement,
            String supplierBizNo,
            String signedXml,
            boolean useCdata
    ) {
        String payload = useCdata
                ? "<![CDATA[" + signedXml + "]]>"
                : escapeXml(signedXml);

        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:nts="%s">
                  <soapenv:Header/>
                  <soapenv:Body>
                    <nts:%s>
                      <nts:SupplierBizNo>%s</nts:SupplierBizNo>
                      <nts:TaxInvoiceXML>%s</nts:TaxInvoiceXML>
                    </nts:%s>
                  </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(
                namespace,
                operationElement,
                digitsOnly(supplierBizNo),
                payload,
                operationElement
        );
    }

    public static String buildListPurchaseRequest(
            String namespace,
            String operationElement,
            String buyerBizNo,
            String sinceDate
    ) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:nts="%s">
                  <soapenv:Header/>
                  <soapenv:Body>
                    <nts:%s>
                      <nts:BuyerBizNo>%s</nts:BuyerBizNo>
                      <nts:SinceDate>%s</nts:SinceDate>
                    </nts:%s>
                  </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(
                namespace,
                operationElement,
                digitsOnly(buyerBizNo),
                sinceDate,
                operationElement
        );
    }

    private static String digitsOnly(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("\\D", "");
    }

    private static String escapeXml(String xml) {
        return xml
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
