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
    public List<PartnerDto> list(@RequestParam(name = "q", required = false) String q) {
        return partnerService.list(null, q);
    }

    @PostMapping
    @Operation(summary = "거래처 등록")
    public PartnerDto create(@Valid @RequestBody CreatePartnerRequest body) {
        return partnerService.create(null, body);
    }
}
