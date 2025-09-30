package com.sparta.tdd.global.jwt.filter;

import com.sparta.tdd.domain.auth.UserDetailsImpl;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j(topic = "JwtAuthenticationFilter")
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider accessTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        String accessToken = TokenResolver.extractAccessToken(request);

        if (accessToken != null && !accessToken.isEmpty()) {
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
        String username = claims.getSubject();
        Long userId = claims.get("userId", Long.class);
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
}
