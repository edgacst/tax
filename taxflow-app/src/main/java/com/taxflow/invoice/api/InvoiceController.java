package com.taxflow.invoice.api;

import com.taxflow.invoice.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices", description = "세금계산서")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    @Operation(summary = "세금계산서 목록")
    public List<InvoiceDto> list(
            @RequestParam(name = "tenantId", required = false) Long tenantId,
            @RequestParam(name = "q", required = false) String q
    ) {
        return invoiceService.list(tenantId, q);
    }

    @GetMapping("/{id}")
    @Operation(summary = "세금계산서 상세")
    public InvoiceDetailDto get(
            @RequestParam(name = "tenantId", required = false) Long tenantId,
            @PathVariable Long id
    ) {
        return invoiceService.getById(tenantId, id);
    }

    @PostMapping
    @Operation(summary = "세금계산서 임시저장")
    public InvoiceDetailDto create(
            @RequestParam(name = "tenantId", required = false) Long tenantId,
            @Valid @RequestBody CreateInvoiceRequest body
    ) {
        return invoiceService.createDraft(tenantId, body);
    }
}
