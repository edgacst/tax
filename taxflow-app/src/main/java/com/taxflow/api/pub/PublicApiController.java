package com.taxflow.api.pub;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/public")
@Tag(name = "Public", description = "인증 없이 호출 가능한 API")
public class PublicApiController {

    @GetMapping("/ping")
    @Operation(summary = "Ping", description = "서비스 가동 및 API 스펙 노출 확인용")
    public Map<String, String> ping() {
        return Map.of(
                "status", "ok",
                "service", "taxflow"
        );
    }
}
