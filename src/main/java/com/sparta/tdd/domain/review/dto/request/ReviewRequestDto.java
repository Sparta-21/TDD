package com.sparta.tdd.domain.review.dto.request;

import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.review.entity.Review;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.user.entity.User;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReviewRequestDto(

        String content,

        @NotNull(message = "음식점 Id는 필수입니다.")
        UUID storeId,

        @NotNull(message = "평점은 필수입니다")
        @Min(value = 1, message = "평점은 1점 이상이어야 합니다")
        @Max(value = 5, message = "평점은 5점 이하여야 합니다")
        Integer rating,

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
