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
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = "가게 조회 응답 DTO")
public record StoreResponseDto(

    @Schema(description = "가게 ID", example = "4f1ed1a0-e7dc-4f7d-a806-412e0e07bfbe")
    UUID id,

    @Schema(description = "가게 이름", example = "홍콩반점")
    String name,

    @Schema(description = "점주 이름", example = "TDD")
    String ownerName,

    @Schema(description = "상점 카테고리", implementation = StoreCategory.class)
    StoreCategory category,

    @Schema(description = "가게 설명", example = "정통 중화요리를 판매하는 홍콩반점입니다.")
    String description,

    @Schema(description = "상점 이미지 URL", example = "https://example.com/images/store1.jpg")
    String imageUrl,

    @Schema(description = "평균 평점", example = "4.5")
    BigDecimal avgRating,

    @Schema(description = "리뷰 개수", example = "128")
    Integer reviewCount,

    @Schema(description = "총 주문 수", example = "3456")
    Long orderCount,

    @ArraySchema(schema = @Schema(implementation = MenuWithStoreResponseDto.class),
        arraySchema = @Schema(description = "가게에 등록된 메뉴 목록"))
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
