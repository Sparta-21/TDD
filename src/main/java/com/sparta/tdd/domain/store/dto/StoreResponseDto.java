package com.sparta.tdd.domain.store.dto;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.sparta.tdd.domain.menu.entity.QMenu;
import com.sparta.tdd.domain.store.entity.QStore;
import com.sparta.tdd.domain.store.enums.StoreCategory;
import java.util.UUID;
import lombok.Builder;

@Builder
public record StoreResponseDto(
    UUID id,
    String name,
    String ownerName,
    StoreCategory category,
    String description,
    String imageUrl,
    MenuResponseDto menu
) {


    public static Expression<StoreResponseDto> qConstructor(QStore store, QMenu menu) {
        return Projections.constructor(
            StoreResponseDto.class,
            store.id,
            store.name,
            store.user.username,
            store.category,
            store.description,
            store.imageUrl,
            Projections.constructor(
                MenuResponseDto.class,
                menu.id,
                menu.name,
                menu.price
            )
        );
    }

}
