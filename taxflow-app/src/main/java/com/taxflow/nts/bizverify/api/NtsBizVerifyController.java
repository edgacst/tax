package com.taxflow.nts.bizverify.api;

import com.taxflow.nts.bizverify.NtsBizVerifyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/nts/biz")
@RequiredArgsConstructor
@Tag(name = "NTS Biz Verify", description = "국세청 사업자등록정보 상태조회·진위확인 (공공데이터포털)")
public class NtsBizVerifyController {

    private final NtsBizVerifyService bizVerifyService;

    @GetMapping("/meta")
    @Operation(summary = "연동 설정 여부", description = "서비스키·enabled 설정 상태 (키 값은 노출하지 않음)")
    public NtsBizVerifyMetaDto meta() {
        return bizVerifyService.meta();
    }

    @PostMapping("/status")
    @Operation(summary = "사업자 상태조회", description = "휴·폐업, 과세유형 등 (최대 100건)")
    public BizStatusResponseDto status(@Valid @RequestBody BizStatusRequest body) {
        return bizVerifyService.queryStatus(body.bizNumbers());
    }

    @PostMapping("/validate")
    @Operation(summary = "사업자 진위확인", description = "사업자번호·개업일·대표자명 등 일치 여부")
    public BizValidateResponseDto validate(@Valid @RequestBody BizValidateRequest body) {
        return bizVerifyService.validate(body);
    }
}
