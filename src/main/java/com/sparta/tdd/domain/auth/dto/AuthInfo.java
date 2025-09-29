package com.sparta.tdd.domain.auth.dto;

public record AuthInfo(
    Long userId,
    String accessToken,
    String refreshToken
) {

}
