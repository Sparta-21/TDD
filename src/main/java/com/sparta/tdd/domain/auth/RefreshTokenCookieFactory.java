package com.sparta.tdd.domain.auth;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseCookie;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RefreshTokenCookieFactory {

    private static final String cookieName = "Refresh-Token";

    public static ResponseCookie create(String refreshToken) {
        return ResponseCookie
            .from(cookieName, refreshToken)
            .httpOnly(true)
            .secure(false)
            .path("/")
            .maxAge(60 * 60 * 24 * 7L)
            .sameSite("Lax")
            .build();
    }

    public static ResponseCookie invalidate() {
        return ResponseCookie
            .from(cookieName, "")
            .httpOnly(true)
            .secure(false)
            .path("/")
            .maxAge(0)
            .sameSite("Lax")
            .build();
    }
}
