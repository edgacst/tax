package com.taxflow.nts.purchase;

import java.time.Instant;
import java.util.List;

/**
 * 국세청 홈택스 매입 전자세금계산서 조회 포트.
 * <p>실서비스: SOAP/REST 클라이언트 구현체를 등록하고, 로컬/테스트에서는 {@link StubHometaxPurchaseInquiryPort} 사용.</p>
 */
public interface HometaxPurchaseInquiryPort {

    /**
     * @param tenantId      테넌트 PK
     * @param sinceExclusive 이 시각 이후(초과)로 발행된 매입분만 조회하는 커서(실 API 파라미터에 매핑)
     * @param tenantBizNo   공급받는자(우리) 사업자번호 — 매입 조회 시 식별자
     */
    List<HometaxPurchaseRow> fetchPurchaseInvoicesSince(Long tenantId, Instant sinceExclusive, String tenantBizNo);
}
