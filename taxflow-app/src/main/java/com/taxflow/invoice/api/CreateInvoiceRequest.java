package com.taxflow.invoice.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record CreateInvoiceRequest(
        @NotNull Long workplaceId,
        @NotNull Long partnerId,
        @NotNull LocalDate issueDate,
        @Size(max = 500) String remark,
        @Size(max = 10) String direction,
        @NotEmpty @Valid List<CreateInvoiceItemRequest> items
) {
}
