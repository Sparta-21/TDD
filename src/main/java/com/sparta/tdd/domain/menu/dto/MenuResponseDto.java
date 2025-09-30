package com.sparta.tdd.domain.menu.dto;

import com.sparta.tdd.domain.menu.entity.Menu;
import java.util.UUID;

public record MenuResponseDto(
    UUID menuId,
    String name,
    String description,
    Integer price,
    String imageUrl,
    Boolean isHidden
) {

    public static MenuResponseDto of(Menu menu) {
        return new MenuResponseDto(
            menu.getId(),
            menu.getName(),
            menu.getDescription(),
            menu.getPrice(),
            menu.getImageUrl(),
            menu.getIsHidden()
        );
    }
}
