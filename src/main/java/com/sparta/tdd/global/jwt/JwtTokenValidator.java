package com.sparta.tdd.global.jwt;

import com.sparta.tdd.domain.auth.service.TokenBlacklistService;
import com.sparta.tdd.global.exception.BusinessException;
import com.sparta.tdd.global.exception.ErrorCode;
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
            throw new BusinessException(ErrorCode.ACCESS_TOKEN_NOT_FOUND);
        }

        if (tokenBlacklistService.isAccessTokenBlacklisted(accessToken)) {
            throw new BusinessException(ErrorCode.ACCESS_TOKEN_BLACKLISTED);
        }

        String tokenType = accessTokenProvider.getTokenType(accessToken);
        if (!"access".equals(tokenType) || !accessTokenProvider.validateToken(accessToken)) {
            throw new BusinessException(ErrorCode.ACCESS_TOKEN_INVALID);
        }
    }

    public void validateRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        if (tokenBlacklistService.isRefreshTokenBlacklisted(refreshToken)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_BLACKLISTED);
        }

        String tokenType = refreshTokenProvider.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType) || !refreshTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }
    }
}
