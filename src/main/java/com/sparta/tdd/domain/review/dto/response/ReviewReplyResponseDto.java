// ReviewReplyResponseDto.java
package com.sparta.tdd.domain.review.dto.response;

import com.sparta.tdd.domain.review.entity.ReviewReply;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "리뷰 답글 응답 DTO")
public record ReviewReplyResponseDto(

        @Schema(description = "답글 ID", example = "550e8400-e29b-41d4-a716-446655440003")
        UUID replyId,

        @Schema(description = "리뷰 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID reviewId,

        @Schema(description = "가게 ID", example = "550e8400-e29b-41d4-a716-446655440001")
        UUID storeId,

        @Schema(description = "사장님 ID", example = "2")
        Long ownerId,

        @Schema(description = "답글 내용", example = "좋은 리뷰 감사합니다!")
        String content,

        @Schema(description = "생성일시", example = "2025-01-15T10:30:00")
        LocalDateTime createdAt,

        @Schema(description = "수정일시", example = "2025-01-15T11:30:00")
        LocalDateTime modifiedAt
) {
    public static ReviewReplyResponseDto from(ReviewReply reply) {
        return new ReviewReplyResponseDto(
                reply.getId(),
                reply.getReviewId(),
                reply.getReview().getStoreId(),
                reply.getOwnerId(),
                reply.getContent(),
                reply.getCreatedAt(),
                reply.getUpdatedAt()
        );
    }
}