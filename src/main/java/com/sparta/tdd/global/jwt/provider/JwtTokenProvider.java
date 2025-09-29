package com.sparta.tdd.global.jwt.provider;

import com.sparta.tdd.domain.user.enums.UserAuthority;
import io.jsonwebtoken.Claims;

public interface JwtTokenProvider {

    String generateToken(String username, Long userId, UserAuthority authority);

    boolean validateToken(String token);

    Claims getClaims(String token);

    String getTokenType(String token);
}
