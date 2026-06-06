package com.taxflow.nts.bizverify.api;

import java.util.List;

public record BizValidateResponseDto(
        String statusCode,
        List<BizValidateItemDto> items
) {
}
