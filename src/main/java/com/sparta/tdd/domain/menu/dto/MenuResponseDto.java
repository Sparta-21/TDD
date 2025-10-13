package com.sparta.tdd.domain.menu.dto;

import com.sparta.tdd.domain.menu.entity.Menu;
import java.util.UUID;
import lombok.Builder;

@Builder
public record MenuResponseDto(
    UUID menuId,
    String name,
    String description,
    Integer price,
    String imageUrl,
    Boolean isHidden
) {

    public static MenuResponseDto from(Menu menu) {
        return MenuResponseDto.builder()
            .menuId(menu.getId())
            .name(menu.getName())
            .description(menu.getDescription())
            .price(menu.getPrice())
            .imageUrl(menu.getImageUrl())
            .isHidden(menu.getIsHidden()).build();
    }
}
