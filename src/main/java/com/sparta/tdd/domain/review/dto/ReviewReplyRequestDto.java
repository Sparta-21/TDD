// ReviewReplyRequestDto.java
package com.sparta.tdd.domain.review.dto;

import com.sparta.tdd.domain.review.entity.Review;
import com.sparta.tdd.domain.review.entity.ReviewReply;
import jakarta.validation.constraints.NotBlank;

public record ReviewReplyRequestDto(
        @NotBlank(message = "답글 내용은 필수입니다.")
        String content
) {
        public ReviewReply toEntity(Review review, Long ownerId) {
        return ReviewReply.builder()
                .review(review)
                .content(this.content)
                .ownerId(ownerId)
                .build();
}}