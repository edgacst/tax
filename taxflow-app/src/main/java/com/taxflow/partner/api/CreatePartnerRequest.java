package com.taxflow.partner.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePartnerRequest(
        @NotBlank @Size(min = 10, max = 10) String bizNo,
        @NotBlank @Size(max = 200) String name,
        @Size(max = 100) String ceo,
        @Size(max = 255) String email,
        @Size(max = 20) String phone,
        boolean favorite
) {
}
