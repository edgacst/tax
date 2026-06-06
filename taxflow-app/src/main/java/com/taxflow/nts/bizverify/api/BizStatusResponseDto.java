package com.taxflow.nts.bizverify.api;

import java.util.List;

public record BizStatusResponseDto(
        String statusCode,
        List<BizStatusItemDto> items
) {
}
