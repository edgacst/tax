package com.taxflow.workplace.api;

public record WorkplaceDto(
        Long id,
        String name,
        String bizNo,
        boolean isDefault,
        String address,
        String ceoName,
        String bizType,
        String bizItem,
        String phone,
        String email
) {
}
