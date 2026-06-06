package com.taxflow.nts.bizverify.api;

public record NtsBizVerifyMetaDto(
        boolean configured,
        boolean enabled,
        String baseUrl
) {
}
