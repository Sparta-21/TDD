package com.sparta.tdd.domain.coupon.controller;

import com.sparta.tdd.domain.auth.UserDetailsImpl;
import com.sparta.tdd.domain.coupon.dto.UserCouponResponseDto;
import com.sparta.tdd.domain.coupon.service.UserCouponService;
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
@PreAuthorize("hasAnyRole('CUSTOMER', 'MASTER')")
public class UserCouponController {

    private final UserCouponService userCouponService;


    @GetMapping("/my/list")
    public ResponseEntity<List<UserCouponResponseDto>> getMyCoupons(
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(userCouponService.getMyCoupons(userDetails.getUserId()));
    }

    @PostMapping("/{couponId}")
    public ResponseEntity<UserCouponResponseDto> createUserCoupon(@PathVariable UUID couponId,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(userCouponService.createUserCoupon(couponId, userDetails.getUserId()));
    }

}
