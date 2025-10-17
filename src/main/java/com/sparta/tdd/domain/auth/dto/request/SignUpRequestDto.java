package com.sparta.tdd.domain.auth.dto.request;

import com.sparta.tdd.domain.user.enums.UserAuthority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(description = "회원가입 요청 DTO")
public record SignUpRequestDto(
    @NotBlank(message = "사용자명은 필수입니다")
    @Pattern(regexp = "^[a-z0-9]{4,10}$", message = "4~10 자의 알파벳 소문자와 숫자만 가능합니다")
    @Schema(description = "사용자명(필수)", example = "user1")
    String username,

    @NotBlank(message = "비밀번호는 필수입니다")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*]).{8,15}$",
        message = "비밀번호는 8~15자의 대소문자, 숫자, 특수문자(!@#$%^&*)를 각각 포함해야 합니다"
    )
    @Schema(description = "비밀번호(필수)", example = "Password1!")
    String password,

    @NotBlank(message = "닉네임 입력은 필수입니다")
    @Schema(description = "닉네임", example = "testUser1")
    String nickname,

    @NotNull(message = "권한 설정은 필수입니다")
    @Schema(description = "권한(CUSTOMER, OWNER, MANAGER, MASTER)", example = "CUSTOMER")
    UserAuthority authority
) {

}
