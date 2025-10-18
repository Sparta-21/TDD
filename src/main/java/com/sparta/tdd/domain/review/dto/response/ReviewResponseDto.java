package com.sparta.tdd.domain.review.dto.response;

import com.sparta.tdd.domain.review.entity.Review;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "리뷰 응답 DTO")
public record ReviewResponseDto(

        @Schema(description = "리뷰 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID reviewId,

        @Schema(description = "가게 ID", example = "550e8400-e29b-41d4-a716-446655440001")
        UUID storeId,

        @Schema(description = "주문 ID", example = "550e8400-e29b-41d4-a716-446655440002")
        UUID orderId,

        @Schema(description = "리뷰 내용", example = "음식이 정말 맛있었어요!")
        String content,

        @Schema(description = "평점 (1~5점)", example = "5")
        Integer rating,

        @Schema(description = "작성자 ID", example = "1")
        Long userId,

        @Schema(description = "리뷰 사진 URL", example = "https://example.com/photo.jpg")
        String photos,

        @Schema(description = "생성일시", example = "2025-01-15T10:30:00")
        LocalDateTime createdAt,

        @Schema(description = "수정일시", example = "2025-01-15T11:30:00")
        LocalDateTime modifiedAt,

        @Schema(description = "답글 정보")
        ReviewReplyInfo reply
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
                null
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