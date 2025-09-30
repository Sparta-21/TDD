package com.sparta.tdd.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.tdd.global.jwt.filter.JwtAuthenticationFilter;
import com.sparta.tdd.global.jwt.filter.JwtExceptionFilter;
import com.sparta.tdd.global.jwt.provider.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class JwtFilterConfig {

    private final ObjectMapper objectMapper;
    private final JwtTokenProvider accessTokenProvider;

    @Bean
    public JwtExceptionFilter jwtExceptionFilter() {
        return new JwtExceptionFilter(objectMapper);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(accessTokenProvider);
    }
}
