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
    public List<InvoiceDto> list(@RequestParam(name = "q", required = false) String q) {
        return invoiceService.list(null, q);
    }

    @GetMapping("/{id}")
    @Operation(summary = "세금계산서 상세")
    public InvoiceDetailDto get(@PathVariable Long id) {
        return invoiceService.getById(null, id);
    }

    @PostMapping
    @Operation(summary = "세금계산서 임시저장")
    public InvoiceDetailDto create(@Valid @RequestBody CreateInvoiceRequest body) {
        return invoiceService.createDraft(null, body);
    }
}
