package com.taxflow.nts.purchase;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 홈택스 연동 전 개발용 스텁. 호출 시마다 0~2건의 가짜 매입 데이터를 생성합니다.
 */
@Component
@Primary
public class StubHometaxPurchaseInquiryPort implements HometaxPurchaseInquiryPort {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Override
    public List<HometaxPurchaseRow> fetchPurchaseInvoicesSince(
            Long tenantId,
            @SuppressWarnings("unused") java.time.Instant sinceExclusive,
            String tenantBizNo
    ) {
        int n = ThreadLocalRandom.current().nextInt(0, 3);
        List<HometaxPurchaseRow> rows = new ArrayList<>();
        String day = LocalDate.now(KST).format(DateTimeFormatter.ISO_LOCAL_DATE);
        long base = System.currentTimeMillis() % 1_000_000;
        for (int i = 0; i < n; i++) {
            long supply = 100_000L * (i + 1) + base % 50_000;
            long tax = supply / 10;
            long total = supply + tax;
            String arn = "STUB" + day.replace("-", "") + tenantId + base + i + ThreadLocalRandom.current().nextInt(1000);
            String arn30 = arn.length() > 30 ? arn.substring(0, 30) : arn;
            int r = ThreadLocalRandom.current().nextInt(100_000_000, 1_000_000_000);
            String supplierBiz = String.format("%010d", r);
            rows.add(
                    new HometaxPurchaseRow(
                            arn30,
                            LocalDate.now(KST).minusDays(i),
                            supplierBiz,
                            "스텁공급자-" + i,
                            tenantBizNo,
                            supply,
                            tax,
                            total,
                            "{\"source\":\"stub\",\"tenantId\":" + tenantId + "}"
                    )
            );
        }
        return rows;
    }
}
