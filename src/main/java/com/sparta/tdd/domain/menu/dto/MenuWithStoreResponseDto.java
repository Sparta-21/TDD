package com.sparta.tdd.domain.menu.dto;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
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
}