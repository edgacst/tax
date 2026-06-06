package com.taxflow.nts.bizverify.api;

public record BizStatusItemDto(
        String bizNo,
        String businessStatus,
        String businessStatusCode,
        String taxType,
        String taxTypeCode,
        String endDate,
        String utccYn,
        boolean registered
) {
}
