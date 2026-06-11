package com.taxflow.nts.submit;

import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * KISA 표준전자세금계산서 v3.0 기반 XML (핵심 필드).
 */
@Component
public class KisaTaxInvoiceXmlGenerator {

    private static final String NS = "urn:kr:or:kec:standard:Tax:ReusableAggregateBusinessInformationEntitySchemaModule:1:0";

    public String generate(TaxInvoiceSubmitContext ctx) {
        String issueDate = ctx.issueDate().format(DateTimeFormatter.BASIC_ISO_DATE);
        StringBuilder items = new StringBuilder();
        for (TaxInvoiceSubmitContext.LineItem line : ctx.items()) {
            items.append("""
                    <TaxInvoiceTradeLineItem>
                      <SequenceNumeric>%d</SequenceNumeric>
                      <InvoiceAmount>%d</InvoiceAmount>
                      <ChargeableUnitQuantity>%s</ChargeableUnitQuantity>
                      <InformationText>%s</InformationText>
                      <NameText>%s</NameText>
                      <PurchaseExpiryDateTime>%s</PurchaseExpiryDateTime>
                      <TotalTax>%d</TotalTax>
                      <UnitPrice>%d</UnitPrice>
                    </TaxInvoiceTradeLineItem>
                    """.formatted(
                    line.seq(),
                    line.amount(),
                    trimDecimal(line.quantity()),
                    esc(line.itemName()),
                    esc(line.itemName()),
                    issueDate,
                    line.tax(),
                    line.unitPrice()
            ));
        }

        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <TaxInvoice xmlns="%s">
                  <ExchangedDocument>
                    <ID>%s</ID>
                    <IssueDateTime>%s</IssueDateTime>
                  </ExchangedDocument>
                  <TaxInvoiceDocument>
                    <IssueID>%s</IssueID>
                    <TypeCode>0101</TypeCode>
                    <DescriptionText>%s</DescriptionText>
                    <IssueDateTime>%s</IssueDateTime>
                    <PurposeCode>02</PurposeCode>
                  </TaxInvoiceDocument>
                  <TaxInvoiceTradeSettlement>
                    <InvoicerParty>
                      <ID>%s</ID>
                      <NameText>%s</NameText>
                      <SpecifiedPerson>
                        <NameText>%s</NameText>
                      </SpecifiedPerson>
                      <SpecifiedAddress>
                        <LineOneText>%s</LineOneText>
                      </SpecifiedAddress>
                      <TypeCode>%s</TypeCode>
                      <ClassificationCode>%s</ClassificationCode>
                    </InvoicerParty>
                    <InvoiceeParty>
                      <ID>%s</ID>
                      <NameText>%s</NameText>
                      <SpecifiedPerson>
                        <NameText>%s</NameText>
                      </SpecifiedPerson>
                      <SpecifiedAddress>
                        <LineOneText>%s</LineOneText>
                      </SpecifiedAddress>
                    </InvoiceeParty>
                    <SpecifiedMonetarySummation>
                      <ChargeTotalAmount>%d</ChargeTotalAmount>
                      <TaxTotalAmount>%d</TaxTotalAmount>
                      <GrandTotalAmount>%d</GrandTotalAmount>
                    </SpecifiedMonetarySummation>
                  </TaxInvoiceTradeSettlement>
                  %s
                </TaxInvoice>
                """.formatted(
                NS,
                esc(ctx.serialNo()),
                issueDate,
                esc(ctx.serialNo()),
                esc(nullToEmpty(ctx.remark())),
                issueDate,
                digits(ctx.supplier().bizNo()),
                esc(ctx.supplier().name()),
                esc(nullToEmpty(ctx.supplier().ceoName())),
                esc(nullToEmpty(ctx.supplier().address())),
                esc(nullToEmpty(ctx.supplier().bizType())),
                esc(nullToEmpty(ctx.supplier().bizItem())),
                digits(ctx.buyer().bizNo()),
                esc(ctx.buyer().name()),
                esc(nullToEmpty(ctx.buyer().ceoName())),
                esc(nullToEmpty(ctx.buyer().address())),
                ctx.supplyAmount(),
                ctx.taxAmount(),
                ctx.grandTotal(),
                items
        );
    }

    private static String esc(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String digits(String bizNo) {
        if (bizNo == null) {
            return "";
        }
        return bizNo.replaceAll("\\D", "");
    }

    private static String trimDecimal(double value) {
        if (value == (long) value) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
}
