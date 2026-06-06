package com.taxflow.dashboard.api;

import java.util.List;

public record DashboardSummaryDto(
        int approvedCount,
        int draftCount,
        long approvedSalesTotal,
        int partnerCount,
        List<InvoiceSummaryDto> recentInvoices
) {
}
