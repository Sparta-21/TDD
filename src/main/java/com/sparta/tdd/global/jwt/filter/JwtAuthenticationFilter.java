package com.sparta.tdd.global.jwt.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.tdd.domain.auth.UserDetailsImpl;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import com.sparta.tdd.global.exception.BusinessException;
import com.sparta.tdd.global.jwt.JwtTokenValidator;
import com.sparta.tdd.global.jwt.RequestTokenExtractor;
import com.sparta.tdd.global.jwt.provider.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j(topic = "JwtAuthenticationFilter")
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider accessTokenProvider;
    private final JwtTokenValidator jwtTokenValidator;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        Optional<String> accessToken = RequestTokenExtractor.extractAccessToken(request);

        // AT가 비었어도 회원가입/로그인 등 비인증 로직이 존재하므로 필터 진행
        if (accessToken.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = accessToken.get();

        try {
            jwtTokenValidator.validateAccessToken(token);
            UserDetailsImpl userDetails = getUserDetails(token);
            setAuthenticationUser(userDetails, request);
            log.info("Authenticated user: {}", userDetails.getUsername());
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "만료된 토큰입니다.");
            return;
        } catch (BusinessException e) {
            log.warn("JWT authentication failed: {}", e.getMessage());
            sendErrorResponse(response, e.getErrorCode().getStatus(), e.getErrorCode().getMessage());
            return;
        } catch (Exception e) {
            log.error("JWT authentication failed: {}", e.getMessage(), e);
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private UserDetailsImpl getUserDetails(String accessToken) {
        Claims claims = accessTokenProvider.getClaims(accessToken);
        Long userId = Long.valueOf(claims.getSubject());
        String username = claims.get("username", String.class);
        UserAuthority authority = UserAuthority.valueOf(claims.get("authority", String.class));

        return new UserDetailsImpl(userId, username, authority);
    }

    private void setAuthenticationUser(UserDetailsImpl userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities()
        );

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message)
        throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String errorJson = new ObjectMapper().writeValueAsString(message);

        response.getWriter().write(errorJson);
    }

}