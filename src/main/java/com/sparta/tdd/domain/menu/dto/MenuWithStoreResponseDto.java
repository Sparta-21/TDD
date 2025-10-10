package com.sparta.tdd.domain.menu.dto;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.sparta.tdd.domain.menu.entity.Menu;
import com.sparta.tdd.domain.menu.entity.QMenu;
import java.util.UUID;
import lombok.Builder;

@Builder
public record MenuWithStoreResponseDto(
    UUID menuId,
    String name,
    String description,
    Integer price,
    String imageUrl,
    Boolean isHidden,
    UUID storeId
) {

    public static Expression<MenuWithStoreResponseDto> qConstructor(QMenu menu) {
        return Projections.constructor(
            MenuWithStoreResponseDto.class,
            menu.id,
            menu.name,
            menu.description,
            menu.price,
            menu.imageUrl,
            menu.isHidden,
            menu.store.id
        );
    }

    public static MenuWithStoreResponseDto from(Menu menu) {
        return MenuWithStoreResponseDto.builder()
            .menuId(menu.getId())
            .name(menu.getName())
            .price(menu.getPrice())
            .description(menu.getDescription())
            .build();
    }
}