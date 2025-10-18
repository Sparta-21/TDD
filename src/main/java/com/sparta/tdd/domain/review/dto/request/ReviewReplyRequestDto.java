// ReviewReplyRequestDto.java
package com.sparta.tdd.domain.review.dto.request;

import com.sparta.tdd.domain.review.entity.Review;
import com.sparta.tdd.domain.review.entity.ReviewReply;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "리뷰 답글 요청 DTO")
public record ReviewReplyRequestDto(
        @NotBlank(message = "답글 내용은 필수입니다.")
        @Schema(description = "답글 내용", example = "좋은 리뷰 감사합니다! 다음에도 방문해주세요.")
        String content
) {
        public ReviewReply toEntity(Review review, Long ownerId) {
        return ReviewReply.builder()
                .review(review)
                .content(this.content)
                .ownerId(ownerId)
                .build();
}}