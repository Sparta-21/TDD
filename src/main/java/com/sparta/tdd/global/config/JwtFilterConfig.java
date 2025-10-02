package com.sparta.tdd.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.tdd.domain.auth.service.TokenBlacklistService;
import com.sparta.tdd.global.jwt.filter.JwtAuthenticationFilter;
import com.sparta.tdd.global.jwt.filter.JwtExceptionFilter;
import com.sparta.tdd.global.jwt.provider.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtFilterConfig {

    private final ObjectMapper objectMapper;
    private final JwtTokenProvider accessTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    JwtFilterConfig(
        ObjectMapper objectMapper,
        @Qualifier("accessTokenProvider") JwtTokenProvider accessTokenProvider,
        TokenBlacklistService tokenBlacklistService
    ) {
        this.objectMapper = objectMapper;
        this.accessTokenProvider = accessTokenProvider;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Bean
    public JwtExceptionFilter jwtExceptionFilter() {
        return new JwtExceptionFilter(objectMapper);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(accessTokenProvider, tokenBlacklistService);
    }
}
