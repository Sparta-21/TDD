// ReviewReplyRequestDto.java
package com.sparta.tdd.domain.review.dto;

import jakarta.validation.constraints.NotBlank;

public record ReviewReplyRequestDto(
        @NotBlank(message = "답글 내용은 필수입니다.")
        String content
) {}