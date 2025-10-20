package com.sparta.tdd.domain.user.controller;

import com.sparta.tdd.domain.auth.UserDetailsImpl;
import com.sparta.tdd.domain.order.dto.OrderResponseDto;
import com.sparta.tdd.domain.review.dto.response.ReviewResponseDto;
import com.sparta.tdd.domain.user.dto.*;
import com.sparta.tdd.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "모든 유저 조회",
            description = """
                    모든 유저의 id, username, password, nickname, authority를 조회합니다.
                    MANAGER나 MASTER가 아니면 조회할 수 없습니다.
                    """)
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
    public ResponseEntity<UserPageResponseDto> getAllUser(Pageable pageable) {
        UserPageResponseDto users = new UserPageResponseDto(userService.getAllUsers(pageable));
        return ResponseEntity.ok(users);
    }

    @Operation(
            summary = "유저 조회",
            description = """
                    특정 유저의 id, username, password, nickname, authority를 조회합니다.
                    """
    )
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDto> getUserByUserId(@PathVariable("userId") Long userId) {
        UserResponseDto user = userService.getUserByUserId(userId);
        return ResponseEntity.ok(user);
    }

    @Operation(
            summary = "유저 닉네임 변경",
            description = """
                    유저의 닉네임을 변경합니다.
                    자신의 닉네임만 변경할 수 있습니다.
                    """)
    @PatchMapping("/{userId}/nickname")
    public ResponseEntity<UserResponseDto> updateUserNickname(@PathVariable("userId") Long userId,
                                              @Valid @RequestBody UserNicknameRequestDto requestDto,
                                              @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserResponseDto responseDto = userService.updateUserNickname(userId, userDetails.getUserId(), requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(
            summary = "유저 비밀번호 변경",
            description = """
                    유저의 비밀번호를 변경합니다. 비밀번호는 8~15자의 대소문자, 숫자, 특수문자를 포함해야 합니다.
                    자신의 비밀번호만 변경할 수 있습니다.
                    """)
    @PatchMapping("/{userId}/password")
    public ResponseEntity<UserResponseDto> updateUserPassword(@PathVariable("userId") Long userId,
                                              @Valid @RequestBody UserPasswordRequestDto requestDto,
                                              @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserResponseDto responseDto = userService.updateUserPassword(userId, userDetails.getUserId(), requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(
            summary = "유저 매니저 권한 부여",
            description = """
                    유저의 권한을 매니저로 변경합니다. MASTER 권한을 가진 유저만 변경할 수 있습니다.
                    """)
    @PatchMapping("/{userId}/authority")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<UserResponseDto> updateManagerAuthorityUser(@PathVariable("userId") Long userId) {
        UserResponseDto responseDto = userService.grantUserManagerAuthority(userId);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(
            summary = "유저 리뷰 목록 조회",
            description = """
                    특정 유저가 작성한 리뷰 목록을 조회합니다. 삭제한 리뷰는 조회할 수 없습니다.
                    """)
    @GetMapping("/{userId}/reviews")
    public ResponseEntity<Page<ReviewResponseDto>> getUserReviewsByUserId(@PathVariable("userId") Long userId,
                                                                          Pageable pageable) {
        Page<ReviewResponseDto> responseDto = userService.getPersonalReviews(userId, pageable);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(
            summary = "유저 주문 목록 조회",
            description = """
                    특정 유저가 주문한 주문 목록을 조회합니다. 삭제한 주문은 조회할 수 없습니다.
                    """)
    @GetMapping("/{userId}/orders")
    public ResponseEntity<Page<OrderResponseDto>> getUserOrdersByUserId(@PathVariable("userId") Long userId,
                                                                        Pageable pageable) {
        Page<OrderResponseDto> responseDto = userService.getPersonalOrders(userId, pageable);
        return ResponseEntity.ok(responseDto);
    }
}
