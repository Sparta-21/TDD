package com.sparta.tdd.domain.auth.controller;

import com.sparta.tdd.domain.auth.RefreshTokenCookieFactory;
import com.sparta.tdd.domain.auth.dto.AuthInfo;
import com.sparta.tdd.domain.auth.dto.request.LoginRequestDto;
import com.sparta.tdd.domain.auth.dto.request.SignUpRequestDto;
import com.sparta.tdd.domain.auth.dto.response.LoginResponse;
import com.sparta.tdd.domain.auth.dto.response.SignUpResponse;
import com.sparta.tdd.domain.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequestDto request) {
        AuthInfo info = authService.signUp(request);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + info.accessToken());
        headers.add(HttpHeaders.SET_COOKIE, RefreshTokenCookieFactory.create(info.refreshToken()).toString());

        return ResponseEntity.ok()
            .headers(headers)
            .body(new SignUpResponse(info.userId()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto request) {
        AuthInfo info = authService.login(request);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + info.accessToken());
        headers.add(HttpHeaders.SET_COOKIE, RefreshTokenCookieFactory.create(info.refreshToken()).toString());

        return ResponseEntity.ok()
            .headers(headers)
            .body(new LoginResponse(info.userId()));
    }
}
