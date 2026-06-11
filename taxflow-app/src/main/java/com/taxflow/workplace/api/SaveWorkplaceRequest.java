package com.taxflow.workplace.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SaveWorkplaceRequest(
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(min = 10, max = 10) String bizNo,
        @Size(max = 500) String address,
        @Size(max = 100) String ceoName,
        @Size(max = 100) String bizType,
        @Size(max = 100) String bizItem,
        @Size(max = 20) String phone,
        @Size(max = 255) String email,
        boolean isDefault
) {
}
