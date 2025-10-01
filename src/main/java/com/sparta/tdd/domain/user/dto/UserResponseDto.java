package com.sparta.tdd.domain.user.dto;

import com.sparta.tdd.domain.user.entity.User;

public record UserResponseDto(
        Long id,
        String username,
        String password,
        String nickname,
        String authority
) {
    public static UserResponseDto from(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getNickname(),
                user.getAuthority().getDescription()
        );
    }
}
