package com.taxflow.workplace.api;

public record WorkplaceDto(
        Long id,
        String name,
        String bizNo,
        boolean isDefault,
        String address
) {
}
