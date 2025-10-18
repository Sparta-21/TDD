package com.sparta.tdd.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원가입 응답 DTO")
public record SignUpResponse(
    @Schema(description = "회원 ID", example = "1")
    Long userId
) {

}