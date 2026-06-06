package com.taxflow.certificate.api;

import com.taxflow.certificate.CertificateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/certificates")
@RequiredArgsConstructor
@Tag(name = "Certificates", description = "공인인증서 메타")
public class CertificateController {

    private final CertificateService certificateService;

    @GetMapping
    @Operation(summary = "인증서 목록")
    public List<CertificateDto> list(@RequestParam(name = "tenantId", required = false) Long tenantId) {
        return certificateService.list(tenantId);
    }
}
