package com.sparta.tdd.domain.auth.controller;

import com.sparta.tdd.domain.auth.RefreshTokenCookieFactory;
import com.sparta.tdd.domain.auth.UserDetailsImpl;
import com.sparta.tdd.domain.auth.dto.AuthInfo;
import com.sparta.tdd.domain.auth.dto.request.LoginRequestDto;
import com.sparta.tdd.domain.auth.dto.request.SignUpRequestDto;
import com.sparta.tdd.domain.auth.dto.response.LoginResponse;
import com.sparta.tdd.domain.auth.dto.response.SignUpResponse;
import com.sparta.tdd.domain.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private static final String TOKEN_PREFIX = "Bearer ";

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequestDto request) {
        AuthInfo info = authService.signUp(request);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, TOKEN_PREFIX + info.accessToken());
        headers.add(HttpHeaders.SET_COOKIE, RefreshTokenCookieFactory.create(info.refreshToken()).toString());

        return ResponseEntity.ok()
            .headers(headers)
            .body(new SignUpResponse(info.userId()));
    }

    @GetMapping("/exists")
    public ResponseEntity<?> checkUsernameExists(@RequestParam(name = "username") String username) {
        authService.checkUsernameExists(username);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto request) {
        AuthInfo info = authService.login(request);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, TOKEN_PREFIX + info.accessToken());
        headers.add(HttpHeaders.SET_COOKIE, RefreshTokenCookieFactory.create(info.refreshToken()).toString());

        return ResponseEntity.ok()
            .headers(headers)
            .body(new LoginResponse(info.userId()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        authService.logout(request);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, RefreshTokenCookieFactory.invalidate().toString());

        return ResponseEntity.ok().headers(headers).build();
    }

    @DeleteMapping("/withdrawal")
    public ResponseEntity<?> withdrawal(HttpServletRequest request,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        authService.withdrawal(userDetails.getUserId());
        authService.logout(request);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, RefreshTokenCookieFactory.invalidate().toString());

        return ResponseEntity.ok().headers(headers).build();
    }

    @PostMapping("/token/reissue")
    public ResponseEntity<?> reissueToken(HttpServletRequest request) {
        AuthInfo info = authService.reissueToken(request);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, TOKEN_PREFIX + info.accessToken());
        headers.add(HttpHeaders.SET_COOKIE, RefreshTokenCookieFactory.create(info.refreshToken()).toString());

        return ResponseEntity.ok()
            .headers(headers)
            .build();
    }
}
