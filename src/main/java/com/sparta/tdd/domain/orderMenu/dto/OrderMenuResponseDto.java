package com.sparta.tdd.domain.orderMenu.dto;

import java.util.UUID;

public record OrderMenuResponseDto(
    UUID id,
    String name,
    Integer price,
    Integer quantity
) {

}
