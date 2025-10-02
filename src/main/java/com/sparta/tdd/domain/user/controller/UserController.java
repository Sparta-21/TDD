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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users")
public class UserController {

    private final UserService userService;

    // 회원 목록 조회
    @GetMapping
    @PreAuthorize("hasRole('ROLE_MASTER') or hasRole('ROLE_MANAGER')")
    @Operation(summary = "모든 유저 조회")
    public ResponseEntity<UserPageResponseDto> getAllUser(Pageable pageable) {
        UserPageResponseDto users = new UserPageResponseDto(userService.getAllUsers(pageable));
        return ResponseEntity.ok(users);
    }

    // 회원 정보 조회
    @GetMapping("/{userId}")
    @Operation(summary = "유저 식별자로 유저 조회")
    public ResponseEntity<UserResponseDto> getUserByUserId(@PathVariable("userId") Long userId) {
        UserResponseDto user = userService.getUserByUserId(userId);
        return ResponseEntity.ok(user);
    }

    // 회원 닉네임 수정
    @PatchMapping("/{userId}/nickname")
    @Operation(summary = "유저 닉네임 변경")
    public ResponseEntity<UserResponseDto> updateUserNickname(@PathVariable("userId") Long userId,
                                              @RequestBody UserNicknameRequestDto requestDto,
                                              @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserResponseDto responseDto = userService.updateUserNickname(userId, userDetails.getUserId(), requestDto);
        return ResponseEntity.ok(responseDto);
    }

    // 회원 비밀번호 수정
    @PatchMapping("/{userId}/password")
    @Operation(summary = "유저 비밀번호 변경")
    public ResponseEntity<UserResponseDto> updateUserPassword(@PathVariable("userId") Long userId,
                                              @Valid @RequestBody UserPasswordRequestDto requestDto,
                                              @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserResponseDto responseDto = userService.updateUserPassword(userId, userDetails.getUserId(), requestDto);
        return ResponseEntity.ok(responseDto);
    }

    // 회원 매니저 권한 부여
    @PatchMapping("/{userId}/authority")
    @PreAuthorize("hasRole('ROLE_MASTER')")
    @Operation(summary = "유저 매니저 권한 부여")
    public ResponseEntity<UserResponseDto> updateManagerAuthorityUser(@PathVariable("userId") Long userId) {
        UserResponseDto responseDto = userService.grantUserManagerAuthority(userId);
        return ResponseEntity.ok(responseDto);
    }
}
