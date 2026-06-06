package com.taxflow.nts.bizverify.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BizValidateRequest(
        @NotBlank @Size(min = 10, max = 12) String bizNo,
        @NotBlank @Size(min = 8, max = 8) String startDt,
        @NotBlank @Size(max = 100) String ceoName,
        @Size(max = 100) String ceoName2,
        @Size(max = 200) String corpName,
        @Size(max = 13) String corpNo,
        @Size(max = 100) String bizSector,
        @Size(max = 100) String bizType,
        @Size(max = 500) String address
) {
}
