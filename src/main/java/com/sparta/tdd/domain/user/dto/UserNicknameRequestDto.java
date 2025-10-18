package com.sparta.tdd.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "회원 닉네임 변경 요청 DTO")
public record UserNicknameRequestDto(
        @NotBlank(message = "닉네임 입력은 필수입니다")
        @Schema(description = "닉네임", example = "Nick")
        String nickname
) {
}
