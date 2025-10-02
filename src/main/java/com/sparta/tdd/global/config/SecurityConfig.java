package com.sparta.tdd.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.tdd.global.jwt.entrypoint.JwtAuthenticationEntryPoint;
import com.sparta.tdd.global.jwt.filter.JwtAuthenticationFilter;
import com.sparta.tdd.global.jwt.filter.JwtExceptionFilter;
import com.sparta.tdd.global.jwt.handler.JwtAccessDeniedHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtExceptionFilter jwtExceptionFilter;
    private final ObjectMapper objectMapper;

    private final String[] readOnlyUrl = {
        "/favicon.ico",
        "/api-docs/**",
        "/v3/api-docs/**",
        "/swagger-ui/**", "/swagger",
    };


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .sessionManagement(sessionManagement ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtExceptionFilter, JwtAuthenticationFilter.class)
            .authorizeHttpRequests(authorizeHttpRequests ->
                authorizeHttpRequests
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers(HttpMethod.GET, readOnlyUrl).permitAll()
                    .requestMatchers(HttpMethod.GET, "/v1/auth/exists").permitAll()
                    .requestMatchers(HttpMethod.POST, "/v1/auth/login", "/v1/auth/signup",
                        "/v1/auth/token/reissue").permitAll()
                    .anyRequest().authenticated())
            .exceptionHandling(exception ->
                exception
                    .accessDeniedHandler(new JwtAccessDeniedHandler(objectMapper))
                    .authenticationEntryPoint(new JwtAuthenticationEntryPoint(objectMapper)));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
