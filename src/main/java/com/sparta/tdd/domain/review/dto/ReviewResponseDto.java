package com.sparta.tdd.domain.review.dto;

import com.sparta.tdd.domain.review.entity.Review;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewResponseDto(
        UUID reviewId,
        UUID storeId,
        String content,
        Integer rating,
        Long userId,
        String photos,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static ReviewResponseDto from(Review review) {
        return new ReviewResponseDto(
                review.getReviewId(),
                review.getStoreId(),
                review.getContent(),
                review.getRating(),
                review.getUserId(),
                review.getImageUrl(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
