package com.taxflow.nts.bizverify.api;

public record BizValidateItemDto(
        String bizNo,
        boolean valid,
        String validCode,
        String message
) {
}
