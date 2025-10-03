package com.sparta.tdd.global.jwt;

import com.sparta.tdd.domain.auth.service.TokenBlacklistService;
import com.sparta.tdd.global.jwt.provider.AccessTokenProvider;
import com.sparta.tdd.global.jwt.provider.RefreshTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenValidator {

    private final AccessTokenProvider accessTokenProvider;
    private final RefreshTokenProvider refreshTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    public void validateAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isEmpty()) {
            throw new IllegalArgumentException("액세스 토큰이 존재하지 않습니다.");
        }

        if (tokenBlacklistService.isAccessTokenBlacklisted(accessToken)) {
            throw new IllegalArgumentException("금지된 액세스 토큰입니다.");
        }

        String tokenType = accessTokenProvider.getTokenType(accessToken);
        if (!"access".equals(tokenType) || !accessTokenProvider.validateToken(accessToken)) {
            throw new IllegalArgumentException("유효하지 않은 액세스 토큰입니다.");
        }
    }

    public void validateRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new IllegalArgumentException("리프레시 토큰이 존재하지 않습니다.");
        }

        if (tokenBlacklistService.isRefreshTokenBlacklisted(refreshToken)) {
            throw new IllegalArgumentException("금지된 리프레시 토큰입니다.");
        }

        String tokenType = refreshTokenProvider.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType) || !refreshTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }
    }
}
