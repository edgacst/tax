package com.taxflow.partner.api;

public record PartnerDto(
        Long id,
        String bizNo,
        String name,
        String ceo,
        String email,
        String phone,
        boolean favorite
) {
}
