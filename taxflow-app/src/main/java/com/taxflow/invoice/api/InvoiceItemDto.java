package com.taxflow.invoice.api;

public record InvoiceItemDto(
        Long id,
        int seq,
        String itemName,
        String spec,
        double quantity,
        long unitPrice,
        long amount,
        long tax
) {
}
