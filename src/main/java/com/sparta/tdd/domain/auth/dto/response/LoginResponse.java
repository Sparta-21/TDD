package com.sparta.tdd.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답 API")
public record LoginResponse(
    @Schema(description = "회원 ID", example = "1")
    Long userId
) {

}
