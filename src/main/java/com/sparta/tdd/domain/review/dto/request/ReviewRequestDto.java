package com.sparta.tdd.domain.review.dto.request;

import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.review.entity.Review;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "리뷰 생성 요청 DTO")
public record ReviewRequestDto(
        @Schema(description = "리뷰 내용", example = "음식이 정말 맛있었어요!")
        String content,

        @NotNull(message = "음식점 Id는 필수입니다.")
        @Schema(description = "가게 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID storeId,

        @NotNull(message = "평점은 필수입니다")
        @Min(value = 1, message = "평점은 1점 이상이어야 합니다")
        @Max(value = 5, message = "평점은 5점 이하여야 합니다")
        @Schema(description = "평점 (1~5점)", example = "5")
        Integer rating,

        @Schema(description = "리뷰 사진 URL", example = "https://example.com/photo.jpg")
        String photos
) {
        public Review toEntity(User user, Store store, Order order) {
                return Review.builder()
                        .user(user)
                        .store(store)
                        .order(order)
                        .rating(this.rating)
                        .imageUrl(this.photos)
                        .content(this.content)
                        .build();
        }
}
