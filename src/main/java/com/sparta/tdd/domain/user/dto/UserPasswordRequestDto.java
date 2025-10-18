package com.sparta.tdd.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "회원 비밀번호 변경 요청 DTO")
public record UserPasswordRequestDto(
        @NotBlank(message = "비밀번호는 필수입니다")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*]).{8,15}$",
                message = "비밀번호는 8~15자의 대소문자, 숫자, 특수문자(!@#$%^&*)를 각각 포함해야 합니다"
        )
        @Schema(description = "비밀번호(필수)", example = "Password1!")
        String password
) {
}
