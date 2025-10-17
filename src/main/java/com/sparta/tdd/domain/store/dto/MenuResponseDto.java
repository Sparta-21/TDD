package com.sparta.tdd.domain.store.dto;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.sparta.tdd.domain.menu.entity.QMenu;
import java.util.UUID;
import lombok.Builder;

@Builder
public record MenuResponseDto(
    UUID id,
    String name,
    Integer price
) {

    public static Expression<MenuResponseDto> qConstructor(QMenu menu) {
        return Projections.constructor(
            MenuResponseDto.class,
            menu.id,
            menu.name,
            menu.price
        );
    }
}