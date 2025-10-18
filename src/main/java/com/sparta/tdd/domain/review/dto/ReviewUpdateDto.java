package com.sparta.tdd.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "리뷰 수정 요청 DTO")
public record ReviewUpdateDto(
        @Schema(description = "수정할 리뷰 내용", example = "정말 맛있었습니다!")
        String content,

        @Min(value = 1, message = "평점은 1점 이상이어야 합니다.")
        @Max(value = 5, message = "평점은 5점 이하여야 합니다.")
        @Schema(description = "수정할 평점 (1~5점)", example = "4")
        Integer rating,

        @Schema(description = "수정할 사진 URL", example = "https://example.com/new-photo.jpg")
        String photos
) {
}
