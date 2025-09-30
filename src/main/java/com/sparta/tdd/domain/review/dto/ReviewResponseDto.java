package com.sparta.tdd.domain.review.dto;

import com.sparta.tdd.domain.review.entity.Review;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewResponseDto(
        UUID reviewId,
        UUID storeId,
        UUID orderId,
        String content,
        Integer rating,
        Long userId,
        String photos,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        ReviewReplyInfo reply  // 답글 정보 추가
) {
    public static ReviewResponseDto from(Review review) {
        return new ReviewResponseDto(
                review.getId(),
                review.getStoreId(),
                review.getOrderId(),
                review.getContent(),
                review.getRating(),
                review.getUserId(),
                review.getImageUrl(),
                review.getCreatedAt(),
                review.getUpdatedAt(),
                null  // 답글 없음
        );
    }

    public static ReviewResponseDto from(Review review, ReviewReplyInfo replyInfo) {
        return new ReviewResponseDto(
                review.getId(),
                review.getStoreId(),
                review.getOrderId(),
                review.getContent(),
                review.getRating(),
                review.getUserId(),
                review.getImageUrl(),
                review.getCreatedAt(),
                review.getUpdatedAt(),
                replyInfo
        );
    }

    // 답글 정보를 담는 내부 레코드
    public record ReviewReplyInfo(
            String content
    ) {}
}