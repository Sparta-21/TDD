package com.sparta.tdd.global.jwt.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.tdd.domain.auth.UserDetailsImpl;
import com.sparta.tdd.domain.auth.service.TokenBlacklistService;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import com.sparta.tdd.global.jwt.TokenResolver;
import com.sparta.tdd.global.jwt.provider.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        String accessToken = TokenResolver.extractAccessToken(request);

        if (accessToken != null && !accessToken.isEmpty()) {
            if (tokenBlacklistService.isAccessTokenBlacklisted(accessToken)) {
                sendErrorResponse(response, HttpStatus.FORBIDDEN, "금지된 액세스 토큰입니다.");
                return;
            }
            
            try {
                String accessTokenType = accessTokenProvider.getTokenType(accessToken);
                if ("access".equals(accessTokenType)) {
                    UserDetailsImpl userDetails = getUserDetails(accessToken);
                    setAuthenticationUser(userDetails, request);
                    log.info("Authenticated user: {}", userDetails.getUsername());
                }
            } catch (ExpiredJwtException e1) {
                throw new ServletException();
            }
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