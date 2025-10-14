package com.sparta.tdd.domain.store.dto;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.sparta.tdd.domain.menu.dto.MenuWithStoreResponseDto;
import com.sparta.tdd.domain.order.entity.QOrder;
import com.sparta.tdd.domain.store.entity.QStore;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.store.enums.StoreCategory;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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
    BigDecimal avgRating,
    Integer reviewCount,
    Long orderCount,
    List<MenuWithStoreResponseDto> menus
) {

    public static StoreResponseDto from(Store store) {
        return StoreResponseDto.builder()
            .id(store.getId())
            .name(store.getName())
            .ownerName(store.getUser().getUsername())
            .category(store.getCategory())
            .description(store.getDescription())
            .imageUrl(store.getImageUrl())
            .avgRating(store.getAvgRating())
            .reviewCount(store.getReviewCount())
            .menus(new ArrayList<>())
            .build();
    }

    public static Expression<StoreResponseDto> qConstructor(QStore store, QOrder order) {
        return Projections.constructor(
            StoreResponseDto.class,
            store.id,
            store.name,
            store.user.username,
            store.category,
            store.description,
            store.imageUrl,
            store.avgRating,
            store.reviewCount,
            JPAExpressions.select(order.count())
                .from(order)
                .where(order.store.id.eq(store.id)),
            ExpressionUtils.as(Expressions.constant(new ArrayList<MenuWithStoreResponseDto>()),
                "menus")
        );
    }

    public StoreResponseDto withMenus(List<MenuWithStoreResponseDto> newMenus) {
        return StoreResponseDto.builder()
            .id(this.id)
            .name(this.name)
            .ownerName(this.ownerName)
            .category(this.category)
            .description(this.description)
            .imageUrl(this.imageUrl)
            .avgRating(this.avgRating)
            .reviewCount(this.reviewCount)
            .orderCount(this.orderCount)
            .menus(newMenus)
            .build();
    }
}
