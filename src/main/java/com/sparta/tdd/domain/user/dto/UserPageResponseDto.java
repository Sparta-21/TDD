package com.sparta.tdd.domain.user.dto;

import org.springframework.data.domain.Page;

public record UserPageResponseDto(
        Page<UserResponseDto> users
) {
}
