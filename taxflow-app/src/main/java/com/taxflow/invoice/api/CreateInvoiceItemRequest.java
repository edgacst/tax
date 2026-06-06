package com.taxflow.invoice.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateInvoiceItemRequest(
        @NotBlank @Size(max = 200) String itemName,
        @Min(0) double quantity,
        @Min(0) long unitPrice
) {
}
