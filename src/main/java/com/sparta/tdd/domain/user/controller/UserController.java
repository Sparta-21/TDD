package com.sparta.tdd.domain.user.controller;

import com.sparta.tdd.domain.auth.UserDetailsImpl;
import com.sparta.tdd.domain.user.dto.*;
import com.sparta.tdd.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users")
public class UserController {

    private final UserService userService;

    // 회원 목록 조회

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "모든 유저 조회")
    public Page<UserResponseDto> getAllUser(Pageable pageable,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return userService.getAllUsers(pageable, userDetails.getUserAuthority());
    }

    // 회원 정보 조회
    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "유저 식별자로 유저 조회")
    public UserResponseDto getUserByUserId(@PathVariable("userId") Long userId) {
        return userService.getUserByUserId(userId);
    }

    // 회원 닉네임 수정
    @PatchMapping("/{userId}/nickname")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "유저 닉네임 변경")
    public UserResponseDto updateUserNickname(@PathVariable("userId") Long userId,
                                              @RequestBody UserNicknameRequestDto requestDto,
                                              @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userService.updateUserNickname(userId, userDetails.getUserId(), requestDto);
    }

    // 회원 비밀번호 수정
    @PatchMapping("/{userId}/password")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "유저 비밀번호 변경")
    public UserResponseDto updateUserPassword(@PathVariable("userId") Long userId,
                                              @Valid @RequestBody UserPasswordRequestDto requestDto,
                                              @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userService.updateUserPassword(userId, userDetails.getUserId(), requestDto);
    }

    // 회원 매니저 권한 부여
    @PatchMapping("/{userId}/authority")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "유저 매니저 권한 부여")
    public UserResponseDto updateManagerAuthorityUser(@PathVariable("userId") Long userId,
                                                      @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userService.grantUserManagerAuthority(userId, userDetails.getUserAuthority());
    }
}
