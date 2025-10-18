package com.sparta.tdd.domain.auth.controller;

import com.sparta.tdd.domain.auth.RefreshTokenCookieFactory;
import com.sparta.tdd.domain.auth.UserDetailsImpl;
import com.sparta.tdd.domain.auth.dto.AuthInfo;
import com.sparta.tdd.domain.auth.dto.request.LoginRequestDto;
import com.sparta.tdd.domain.auth.dto.request.SignUpRequestDto;
import com.sparta.tdd.domain.auth.dto.response.LoginResponse;
import com.sparta.tdd.domain.auth.dto.response.SignUpResponse;
import com.sparta.tdd.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "인증 API")
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private static final String TOKEN_PREFIX = "Bearer ";

    @Operation(
        summary = "회원가입",
        description = """
            username, password, nickname, authority를 받아 회원가입을 진행합니다.\n
            해당 password는 암호화되어 저장됩니다.\n
            응답 헤더로 AccessToken을 응답하며, 쿠키로 RefreshToken이 설정됩니다.
            """
    )
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

    @Operation(
        summary = "유저이름 중복확인",
        description = "해당 파라미터로 들어온 username이 중복되는지 체크합니다. 중복 시 예외가 발생합니다."
    )
    @GetMapping("/exists")
    public ResponseEntity<?> checkUsernameExists(@RequestParam(name = "username") String username) {
        authService.checkUsernameExists(username);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "로그인",
        description = """
            username, password 와 일치하는 회원으로 로그인합니다.\n
            응답 헤더로 AccessToken을 응답하며, 쿠키로 RefreshToken이 설정됩니다.
            """
    )
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

    @Operation(
        summary = "로그아웃",
        description = """
            로그인 되어 있는 회원의 로그아웃을 진행합니다. 로그아웃 시 현재 발급된 토큰은 블랙리스트 토큰으로 관리됩니다.\n
            RefreshToken의 경우 쿠키를 무효화합니다.
            """
    )
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        authService.logout(request);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, RefreshTokenCookieFactory.invalidate().toString());

        return ResponseEntity.ok().headers(headers).build();
    }

    @Operation(
        summary = "회원 탈퇴",
        description = """
            로그인 되어 있는 회원의 탈퇴를 진행합니다. 회원탈퇴 시 관련된 데이터의 경우 soft delete로 진행됩니다.\n
            관련 데이터가 모두 제거된 이후, 로그아웃을 진행해 토큰을 무효화하게 됩니다.
            """
    )
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

    @Operation(
        summary = "토큰 재발급",
        description = """
            유효한 RefreshToken을 이용해 AccessToken과 RefreshToken 재발급을 진행합니다.\n
            사용된 AccessToken 및 RefreshToken은 무효화 되며, RefreshToken의 경우 기존의 남은 기간만큼 유효하게 됩니다.
            """
    )
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
