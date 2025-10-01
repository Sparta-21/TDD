package com.sparta.tdd.domain.menu.dto;

public record MenuRequestDto(
    String name,
    String description,
    Integer price,
    String imageUrl
) {

}
