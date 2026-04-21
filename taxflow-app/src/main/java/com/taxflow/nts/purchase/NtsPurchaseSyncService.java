package com.taxflow.nts.purchase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.taxflow.nts.purchase.api.PurchaseReceiptDto;
import com.taxflow.nts.purchase.api.SyncResponseDto;
import com.taxflow.nts.purchase.api.SyncRunDto;
import com.taxflow.tenant.Tenant;
import com.taxflow.tenant.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NtsPurchaseSyncService {

    private final TenantRepository tenantRepository;
    private final NtsSyncRunRepository syncRunRepository;
    private final PurchaseInvoiceReceiptRepository receiptRepository;
    private final HometaxPurchaseInquiryPort inquiryPort;
    private final ObjectMapper objectMapper;

    @Transactional
    public SyncResponseDto sync(Long tenantIdOrNull) {
        Long tenantId = resolveTenantId(tenantIdOrNull);
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "tenant not found"));

        NtsSyncRun run = syncRunRepository.save(
                NtsSyncRun.builder()
                        .tenant(tenant)
                        .syncType("PURCHASE_POLL")
                        .status(NtsSyncRunStatus.RUNNING)
                        .recordsFetched(0)
                        .recordsInserted(0)
                        .build()
        );

        try {
            Instant since = receiptRepository.findLatestSyncedAt(tenantId).orElse(Instant.EPOCH);
            List<HometaxPurchaseRow> rows = inquiryPort.fetchPurchaseInvoicesSince(
                    tenantId,
                    since,
                    tenant.getBizNumber()
            );
            int fetched = rows.size();
            int inserted = 0;
            for (HometaxPurchaseRow row : rows) {
                if (receiptRepository.existsByTenant_IdAndNtsApprovalNumber(tenantId, row.ntsApprovalNumber())) {
                    continue;
                }
                JsonNode raw = toJson(row.rawJsonPreview());
                PurchaseInvoiceReceipt entity = PurchaseInvoiceReceipt.builder()
                        .tenant(tenant)
                        .ntsApprovalNumber(row.ntsApprovalNumber())
                        .issueDate(row.issueDate())
                        .supplierBizNo(row.supplierBizNo())
                        .supplierName(row.supplierName())
                        .buyerBizNo(row.buyerBizNo())
                        .supplyAmount(row.supplyAmount())
                        .taxAmount(row.taxAmount())
                        .totalAmount(row.totalAmount())
                        .direction("receive")
                        .rawJson(raw)
                        .syncedAt(Instant.now())
                        .syncRun(run)
                        .build();
                receiptRepository.save(entity);
                inserted++;
            }
            run.setStatus(NtsSyncRunStatus.SUCCESS);
            run.setRecordsFetched(fetched);
            run.setRecordsInserted(inserted);
            run.setCompletedAt(Instant.now());
            syncRunRepository.save(run);
            return new SyncResponseDto(run.getId(), fetched, inserted, "동기화 완료 (스텁 또는 실연동 클라이언트)");
        } catch (Exception ex) {
            run.setStatus(NtsSyncRunStatus.FAILED);
            run.setErrorMessage(ex.getMessage());
            run.setCompletedAt(Instant.now());
            syncRunRepository.save(run);
            throw ex;
        }
    }

    private JsonNode toJson(String preview) {
        try {
            return objectMapper.readTree(preview);
        } catch (Exception e) {
            ObjectNode n = objectMapper.createObjectNode();
            n.put("preview", preview);
            return n;
        }
    }

    @Transactional(readOnly = true)
    public List<PurchaseReceiptDto> listReceipts(Long tenantIdOrNull) {
        Long tenantId = resolveTenantId(tenantIdOrNull);
        assertTenant(tenantId);
        return receiptRepository.findAllForTenant(tenantId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SyncRunDto> listRuns(Long tenantIdOrNull) {
        Long tenantId = resolveTenantId(tenantIdOrNull);
        assertTenant(tenantId);
        return syncRunRepository.findRecentByTenant(tenantId, PageRequest.of(0, 10)).stream()
                .map(this::toRunDto)
                .toList();
    }

    private Long resolveTenantId(Long tenantIdOrNull) {
        if (tenantIdOrNull != null) {
            return tenantIdOrNull;
        }
        return tenantRepository.findBySchemaName("tenant_demo")
                .map(Tenant::getId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "tenantId 파라미터를 주거나, tenant_demo 시드가 필요합니다."
                ));
    }

    private void assertTenant(Long tenantId) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "tenant not found");
        }
    }

    private PurchaseReceiptDto toDto(PurchaseInvoiceReceipt e) {
        return new PurchaseReceiptDto(
                e.getId(),
                e.getNtsApprovalNumber(),
                e.getIssueDate(),
                e.getSupplierBizNo(),
                e.getSupplierName(),
                e.getBuyerBizNo(),
                e.getSupplyAmount(),
                e.getTaxAmount(),
                e.getTotalAmount(),
                e.getSyncedAt(),
                e.getSyncRun() != null ? e.getSyncRun().getId() : null
        );
    }

    private SyncRunDto toRunDto(NtsSyncRun r) {
        return new SyncRunDto(
                r.getId(),
                r.getStatus(),
                r.getRecordsFetched(),
                r.getRecordsInserted(),
                r.getErrorMessage(),
                r.getStartedAt(),
                r.getCompletedAt()
        );
    }
}
