package com.sparta.tdd.global.jwt.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sparta.tdd.global.exception.BusinessException;
import com.sparta.tdd.global.exception.ErrorCode;
import com.sparta.tdd.global.jwt.JwtTokenValidator;
import com.sparta.tdd.global.jwt.provider.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider accessTokenProvider;

    @Mock
    private JwtTokenValidator jwtTokenValidator;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;


    @Test
    @DisplayName("토큰이 없는 경우 필터를 통과하고 다음 필터로 진행")
    void doFilterInternal_noToken_shouldContinue() throws Exception {
        // given
        when(request.getHeader("Authorization")).thenReturn(null);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenValidator, never()).validateAccessToken(anyString());
    }

    @Test
    @DisplayName("유효한 토큰인 경우 - 인증 성공")
    void doFilterInternal_validToken_shouldAuthenticate() throws Exception {
        // given
        String token = "validToken";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        Claims claims = Jwts
            .claims()
            .subject("1")
            .add("tokenType", "access")
            .add("username", "testUser")
            .add("authority", "CUSTOMER")
            .build();
        when(accessTokenProvider.getClaims(token)).thenReturn(claims);

        // validateAccessToken은 정상 동작 (예외 없음)
        doNothing().when(jwtTokenValidator).validateAccessToken(token);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(jwtTokenValidator).validateAccessToken(token);
        verify(filterChain).doFilter(request, response);

        // SecurityContext에 인증 정보가 설정되었는지 확인
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.isAuthenticated()).isTrue();
    }

    @Test
    @DisplayName("만료된 토큰인 경우 - 401 에러 반환")
    void doFilterInternal_expiredToken_shouldReturnUnauthorized() throws Exception {
        // given
        String token = "expiredToken";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        doThrow(new ExpiredJwtException(null, null, "Expired"))
            .when(jwtTokenValidator).validateAccessToken(token);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(filterChain, never()).doFilter(request, response);
    }


    @Test
    @DisplayName("블랙리스트 토큰인 경우 - 401 에러 반환")
    void doFilterInternal_blacklistedToken_shouldReturnForbidden() throws Exception {
        // given
        String token = "blacklistedToken";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        doThrow(new BusinessException(ErrorCode.ACCESS_TOKEN_BLACKLISTED))
            .when(jwtTokenValidator).validateAccessToken(token);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(filterChain, never()).doFilter(request, response);
    }


    @Test
    @DisplayName("유효하지 않은 토큰인 경우 - 401 에러 반환")
    void doFilterInternal_invalidToken_shouldReturnUnauthorized() throws Exception {
        // given
        String token = "invalidToken";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        doThrow(new BusinessException(ErrorCode.ACCESS_TOKEN_INVALID))
            .when(jwtTokenValidator).validateAccessToken(token);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(filterChain, never()).doFilter(request, response);
    }
}
