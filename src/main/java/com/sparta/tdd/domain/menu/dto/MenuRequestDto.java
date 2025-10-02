package com.sparta.tdd.domain.menu.dto;

import lombok.Builder;

@Builder
public record MenuRequestDto(
    String name,
    String description,
    Integer price,
    String imageUrl
) {

}
