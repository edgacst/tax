package com.taxflow.nts.purchase.api;

import com.taxflow.nts.purchase.NtsPurchaseSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 홈택스 매입 전자세금계산서 동기화 API (JWT + 테넌트 격리). */
@RestController
@RequestMapping("/api/v1/nts/purchases")
@RequiredArgsConstructor
@Tag(name = "NTS Purchase", description = "홈택스 매입 수신·동기화")
public class NtsPurchaseController {

    private final NtsPurchaseSyncService syncService;

    @PostMapping("/sync")
    @Operation(summary = "매입 동기화 실행", description = "홈택스(또는 스텁)에서 매입분을 조회해 DB에 적재합니다.")
    public SyncResponseDto sync() {
        return syncService.sync(null);
    }

    @GetMapping
    @Operation(summary = "매입 수신 목록")
    public List<PurchaseReceiptDto> list() {
        return syncService.listReceipts(null);
    }

    @GetMapping("/sync/runs")
    @Operation(summary = "동기화 실행 이력")
    public List<SyncRunDto> runs() {
        return syncService.listRuns(null);
    }
}
