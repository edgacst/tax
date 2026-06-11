package com.taxflow.certificate.api;

import com.taxflow.certificate.CertificateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/certificates")
@RequiredArgsConstructor
@Tag(name = "Certificates", description = "공인인증서")
public class CertificateController {

    private final CertificateService certificateService;

    @GetMapping
    @Operation(summary = "인증서 목록")
    public List<CertificateDto> list() {
        return certificateService.list();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "인증서 등록 (PKCS#12)")
    public CertificateDto upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("password") String password
    ) {
        return certificateService.upload(file, password);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "인증서 삭제")
    public void delete(@PathVariable Long id) {
        certificateService.delete(id);
    }
}
