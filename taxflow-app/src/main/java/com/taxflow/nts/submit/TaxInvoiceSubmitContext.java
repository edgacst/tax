package com.taxflow.nts.submit;

import java.time.LocalDate;
import java.util.List;

public record TaxInvoiceSubmitContext(
        Long invoiceId,
        String serialNo,
        LocalDate issueDate,
        String status,
        String direction,
        String remark,
        Party supplier,
        Party buyer,
        List<LineItem> items,
        long supplyAmount,
        long taxAmount,
        long grandTotal
) {
    public record Party(
            String bizNo,
            String name,
            String ceoName,
            String address,
            String bizType,
            String bizItem
    ) {
    }

    public record LineItem(
            int seq,
            String itemName,
            double quantity,
            long unitPrice,
            long amount,
            long tax
    ) {
    }
}
