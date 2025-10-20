package com.sparta.tdd.domain.coupon.controller;

import com.sparta.tdd.domain.auth.UserDetailsImpl;
import com.sparta.tdd.domain.coupon.dto.UserCouponResponseDto;
import com.sparta.tdd.domain.coupon.service.UserCouponService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/user/coupon")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CUSTOMER', 'MANAGER', 'MASTER')")
public class UserCouponController {

    private final UserCouponService userCouponService;

    @Operation(
        summary = "내 쿠폰 목록 조회",
        description = "사용자의 쿠폰 목록을 조회합니다. CUSTOMER와 MASTER 권한만 가능하며, ACTIVE 상태 쿠폰부터 최신 발급 순 "
            + "> USED/EXPIRED 상태 쿠폰 최신 발급 순으로 조회됩니다. 만료 후 7일이 지난 쿠폰은 조회되지 않습니다."
    )
    @GetMapping("/my/list")
    public ResponseEntity<List<UserCouponResponseDto>> getMyCoupons(
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(userCouponService.getMyCoupons(userDetails.getUserId()));
    }

    @Operation(
        summary = "유저쿠폰 발급",
        description = "쿠폰을 발급하여 유저쿠폰을 생성합니다. CUSTOMER와 MASTER 권한만 가능하며, 발급된 유저쿠폰의 최초 CouponStatus는 ACTIVE 입니다."
    )
    @PostMapping("/{couponId}")
    public ResponseEntity<UserCouponResponseDto> createUserCoupon(@PathVariable UUID couponId,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(userCouponService.createUserCoupon(couponId, userDetails.getUserId()));
    }

}
