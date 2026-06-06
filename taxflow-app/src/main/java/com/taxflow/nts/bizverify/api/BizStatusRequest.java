package com.taxflow.nts.bizverify.api;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BizStatusRequest(
        @NotEmpty @Size(max = 100) List<@Size(min = 10, max = 12) String> bizNumbers
) {
}
