package com.sparta.tdd.domain.store.dto;

import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.store.enums.StoreCategory;
import java.math.BigDecimal;
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
    Integer reviewCount
) {

    public static StoreResponseDto of(Store store) {
        return StoreResponseDto.builder()
            .id(store.getId())
            .name(store.getName())
            .ownerName(store.getUser().getUsername())
            .category(store.getCategory())
            .description(store.getDescription())
            .imageUrl(store.getImageUrl())
            .avgRating(store.getAvgRating())
            .reviewCount(store.getReviewCount())
            .build();
    }
}
