package com.sparta.tdd.global.jwt.provider;

import com.sparta.tdd.domain.user.enums.UserAuthority;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenProvider implements JwtTokenProvider {

    @Value("${jwt.refresh.secret}")
    private String refreshSecretKey;
    @Value("${jwt.refresh.expiration}")
    private Long refreshExpiration;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(refreshSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateToken(String username, Long userId, UserAuthority authority) {
        return Jwts
            .builder()
            .header()
            .type("JWT")
            .and()
            .issuer("TDD-BE")
            .subject(userId.toString())
            .claim("tokenType", "refresh")
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
            .signWith(key)
            .compact();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts
                .parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Claims getClaims(String token) {
        return Jwts
            .parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    @Override
    public String getTokenType(String token) {
        return Jwts
            .parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .get("tokenType", String.class);
    }

    // reissue 시에만 사용되는 메소드. RT가 재발급 되더라도 유효기간은 동일하게 발급된다.
    public String generateReissueToken(Long userId, Date expiration) {
        return Jwts
            .builder()
            .header()
            .type("JWT")
            .and()
            .issuer("TDD-BE")
            .subject(userId.toString())
            .claim("tokenType", "refresh")
            .issuedAt(new Date())
            .expiration(expiration)
            .signWith(key)
            .compact();
    }

    public Date getExpiration(String token) {
        return Jwts
            .parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getExpiration();
    }
}
