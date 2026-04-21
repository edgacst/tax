package com.taxflow.nts.purchase.api;

import com.taxflow.nts.purchase.NtsPurchaseSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 홈택스 매입 전자세금계산서 동기화 API.
 * <p>프론트·로컬 개발 편의를 위해 {@code /api/v1/nts/**} 는 현재 보안 필터 밖(ignoring)에 둡니다.
 * 운영 전 JWT + 테넌트 검증으로 옮기세요.</p>
 */
@RestController
@RequestMapping("/api/v1/nts/purchases")
@RequiredArgsConstructor
@Tag(name = "NTS Purchase", description = "홈택스 매입 수신·동기화")
public class NtsPurchaseController {

    private final NtsPurchaseSyncService syncService;

    @PostMapping("/sync")
    @Operation(summary = "매입 동기화 실행", description = "홈택스(또는 스텁)에서 매입분을 조회해 DB에 적재합니다.")
    public SyncResponseDto sync(@RequestParam(name = "tenantId", required = false) Long tenantId) {
        return syncService.sync(tenantId);
    }

    @GetMapping
    @Operation(summary = "매입 수신 목록")
    public List<PurchaseReceiptDto> list(@RequestParam(name = "tenantId", required = false) Long tenantId) {
        return syncService.listReceipts(tenantId);
    }

    @GetMapping("/sync/runs")
    @Operation(summary = "동기화 실행 이력")
    public List<SyncRunDto> runs(@RequestParam(name = "tenantId", required = false) Long tenantId) {
        return syncService.listRuns(tenantId);
    }
}
