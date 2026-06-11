package com.taxflow.nts.soap;

import com.taxflow.nts.purchase.HometaxPurchaseRow;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 매입 세금계산서 목록 SOAP 응답 파싱 (반복 블록 또는 개별 태그).
 */
public final class NtsSoapPurchaseResponseParser {

    private static final Pattern ROW_BLOCK = Pattern.compile(
            "<(?:\\w+:)?PurchaseTaxInvoice[^>]*>(.*?)</(?:\\w+:)?PurchaseTaxInvoice>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private NtsSoapPurchaseResponseParser() {
    }

    public static List<HometaxPurchaseRow> parse(String body, String defaultBuyerBizNo) {
        if (body == null || body.isBlank()) {
            return List.of();
        }
        List<HometaxPurchaseRow> rows = new ArrayList<>();
        Matcher m = ROW_BLOCK.matcher(body);
        while (m.find()) {
            String block = m.group(1);
            HometaxPurchaseRow row = parseRow(block, defaultBuyerBizNo);
            if (row != null) {
                rows.add(row);
            }
        }
        if (rows.isEmpty()) {
            HometaxPurchaseRow single = parseRow(body, defaultBuyerBizNo);
            if (single != null) {
                rows.add(single);
            }
        }
        return rows;
    }

    private static HometaxPurchaseRow parseRow(String block, String defaultBuyerBizNo) {
        String approval = NtsSoapResponseParser.firstTagValue(block, List.of(
                "ApprovalNumber", "approvalNumber", "NTSSendKey", "ntsSendKey"
        ));
        if (approval == null || approval.isBlank()) {
            return null;
        }
        String issueDateStr = NtsSoapResponseParser.firstTagValue(block, List.of(
                "IssueDate", "issueDate", "IssueDateTime", "issueDateTime"
        ));
        LocalDate issueDate = parseDate(issueDateStr);
        String supplierBiz = NtsSoapResponseParser.firstTagValue(block, List.of(
                "SupplierBizNo", "supplierBizNo", "InvoicerID", "invoicerID"
        ));
        String supplierName = NtsSoapResponseParser.firstTagValue(block, List.of(
                "SupplierName", "supplierName", "InvoicerName", "invoicerName"
        ));
        String buyerBiz = NtsSoapResponseParser.firstTagValue(block, List.of(
                "BuyerBizNo", "buyerBizNo", "InvoiceeID", "invoiceeID"
        ));
        if (buyerBiz == null || buyerBiz.isBlank()) {
            buyerBiz = defaultBuyerBizNo;
        }
        long supply = parseLong(NtsSoapResponseParser.firstTagValue(block, List.of(
                "SupplyAmount", "supplyAmount", "ChargeTotalAmount", "chargeTotalAmount"
        )));
        long tax = parseLong(NtsSoapResponseParser.firstTagValue(block, List.of(
                "TaxAmount", "taxAmount", "TaxTotalAmount", "taxTotalAmount"
        )));
        long total = parseLong(NtsSoapResponseParser.firstTagValue(block, List.of(
                "TotalAmount", "totalAmount", "GrandTotalAmount", "grandTotalAmount"
        )));
        if (total == 0 && supply > 0) {
            total = supply + tax;
        }
        return new HometaxPurchaseRow(
                approval.trim(),
                issueDate,
                digits(supplierBiz),
                supplierName != null ? supplierName : "",
                digits(buyerBiz),
                supply,
                tax,
                total,
                "{\"source\":\"soap\"}"
        );
    }

    private static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return LocalDate.now();
        }
        String digits = value.replaceAll("\\D", "");
        if (digits.length() >= 8) {
            return LocalDate.parse(digits.substring(0, 8), DateTimeFormatter.BASIC_ISO_DATE);
        }
        try {
            return LocalDate.parse(value.substring(0, Math.min(10, value.length())));
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    private static long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Long.parseLong(value.replaceAll("[^0-9-]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String digits(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("\\D", "");
    }
}
