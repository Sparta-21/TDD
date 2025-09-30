// ReviewReplyResponseDto.java
package com.sparta.tdd.domain.review.dto;

import com.sparta.tdd.domain.review.entity.ReviewReply;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewReplyResponseDto(
        UUID replyId,
        UUID reviewId,
        UUID storeId,
        Long ownerId,
        String content,
        LocalDateTime createdAt,
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