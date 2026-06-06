package com.taxflow.partner.api;

import com.taxflow.partner.PartnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/partners")
@RequiredArgsConstructor
@Tag(name = "Partners", description = "거래처 마스터")
public class PartnerController {

    private final PartnerService partnerService;

    @GetMapping
    @Operation(summary = "거래처 목록")
    public List<PartnerDto> list(
            @RequestParam(name = "tenantId", required = false) Long tenantId,
            @RequestParam(name = "q", required = false) String q
    ) {
        return partnerService.list(tenantId, q);
    }

    @PostMapping
    @Operation(summary = "거래처 등록")
    public PartnerDto create(
            @RequestParam(name = "tenantId", required = false) Long tenantId,
            @Valid @RequestBody CreatePartnerRequest body
    ) {
        return partnerService.create(tenantId, body);
    }
}
