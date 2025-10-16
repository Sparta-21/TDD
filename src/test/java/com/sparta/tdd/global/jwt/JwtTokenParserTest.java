package com.sparta.tdd.global.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class JwtTokenParserTest {

    @Nested
    @DisplayName("extractAccessToken 테스트")
    class extractAccessTokenTest {

        @Test
        @DisplayName("성공 - Bearer로 시작하는 헤더")
        void extractAccessToken_success() {
            // given
            String authorizationHeader = "Bearer accessToken";

            // when
            Optional<String> result = JwtTokenParser.extractAccessToken(authorizationHeader);

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo("accessToken");
        }

        @Test
        @DisplayName("실패 - 헤더 존재하지 않음")
        void extractAccessToken_fail_notExistHeader() {
            // given
            String authorizationHeader = null;

            // when
            Optional<String> result = JwtTokenParser.extractAccessToken(authorizationHeader);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("실패 - Bearer 토큰이 아님")
        void extractAccessToken_fail_notBearerToken() {
            // given
            String authorizationHeader = "accessToken";

            // when
            Optional<String> result = JwtTokenParser.extractAccessToken(authorizationHeader);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("실패 - 빈 헤더 문자열")
        void extractAccessToken_fail_EmptyHeaderValue() {
            // given
            String authorizationHeader = "";

            // when
            Optional<String> result = JwtTokenParser.extractAccessToken(authorizationHeader);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("실패 - 토큰 값이 존재하지 않음")
        void extractAccessToken_fail_emptyTokenValue() {
            // given
            String authorizationHeader = "Bearer ";

            // when
            Optional<String> result = JwtTokenParser.extractAccessToken(authorizationHeader);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("실패 - 대소문자 구분함")
        void extractAccessToken_fail_notIgnoreCase() {
            // given
            String authorizationHeader = "bearer accessToken";

            // when
            Optional<String> result = JwtTokenParser.extractAccessToken(authorizationHeader);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("extractRefreshToken 테스트")
    class extractRefreshTokenTest {

        @Test
        @DisplayName("성공 - Refresh-Token 쿠키 존재")
        void extractRefreshToken_success() {
            // given
            Map<String, String> cookies = Map.of("Refresh-Token", "refreshToken");

            // when
            Optional<String> result = JwtTokenParser.extractRefreshToken(cookies);

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo("refreshToken");
        }

        @Test
        @DisplayName("성공 - 쿠키가 여러개 있어도 Refresh-Token 쿠키 추출")
        void extractRefreshToken_success_with_multiple_cookie() {
            // given
            Map<String, String> cookies = Map.of(
                "TestCookie", "testCookie",
                "Refresh-Token", "refreshToken",
                "Refresh-Token2", "invalid",
                "TestCookie2", "testCookie2"
            );

            // when
            Optional<String> result = JwtTokenParser.extractRefreshToken(cookies);

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo("refreshToken");
        }

        @Test
        @DisplayName("실패 - null 쿠키 맵")
        void extractRefreshToken_fail_nullCookies() {
            // given
            Map<String, String> cookies = null;

            // when
            Optional<String> result = JwtTokenParser.extractRefreshToken(cookies);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("실패 - 빈 쿠키 맵")
        void extractRefreshToken_fail_emptyCookies() {
            // given
            Map<String, String> cookies = Map.of();

            // when
            Optional<String> result = JwtTokenParser.extractRefreshToken(cookies);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("실패 - Refresh-Token 쿠키 없음")
        void extractRefreshToken_fail_noRefreshToken() {
            // given
            Map<String, String> cookies = Map.of(
                "TestCookie", "testCookie",
                "Refresh-Token2", "invalid",
                "TestCookie2", "testCookie2"
            );

            // when
            Optional<String> result = JwtTokenParser.extractRefreshToken(cookies);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("실패 - 쿠키 이름 대소문자 구분")
        void extractRefreshToken_caseSensitive() {
            // given
            Map<String, String> cookies = Map.of("refresh-token", "refreshToken");  // 소문자

            // when
            Optional<String> result = JwtTokenParser.extractRefreshToken(cookies);

            // then
            assertThat(result).isEmpty();  // 대소문자 구분함
        }

        @Test
        @DisplayName("실패 - 쿠키 값이 null")
        void extractRefreshToken_nullValue() {
            // given
            Map<String, String> cookies = new HashMap<>();
            cookies.put("Refresh-Token", null);

            // when
            Optional<String> result = JwtTokenParser.extractRefreshToken(cookies);

            // then
            assertThat(result).isEmpty();  // null 값은 empty 반환
        }

        @Test
        @DisplayName("실패 - 쿠키 값이 빈 문자열")
        void extractRefreshToken_emptyValue() {
            // given
            Map<String, String> cookies = Map.of("Refresh-Token", "");

            // when
            Optional<String> result = JwtTokenParser.extractRefreshToken(cookies);

            // then
            assertThat(result).isEmpty();
        }
    }
}
