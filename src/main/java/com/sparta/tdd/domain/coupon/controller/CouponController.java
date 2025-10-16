package com.sparta.tdd.domain.coupon.controller;

import com.sparta.tdd.domain.auth.UserDetailsImpl;
import com.sparta.tdd.domain.coupon.dto.CouponRequestDto;
import com.sparta.tdd.domain.coupon.dto.CouponResponseDto;
import com.sparta.tdd.domain.coupon.service.CouponService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/coupon")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping("/list/{storeId}")
    public ResponseEntity<List<CouponResponseDto>> getStoreCoupons(@PathVariable UUID storeId) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(couponService.getStoreCoupons(storeId));
    }

    @PreAuthorize("hasAnyRole('OWNER', 'MASTER')")
    @PostMapping("/{storeId}")
    public ResponseEntity<CouponResponseDto> createStoreCoupon(@PathVariable UUID storeId,
        @Valid @RequestBody CouponRequestDto dto,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(couponService.createStoreCoupon(storeId, dto, userDetails.getUserId()));
    }

    @PreAuthorize("hasAnyRole('MASTER')")
    @PostMapping("/master")
    public ResponseEntity<CouponResponseDto> createMasterCoupon(
        @Valid @RequestBody CouponRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(couponService.createMasterCoupon(dto));
    }

    @PreAuthorize("hasAnyRole('OWNER', 'MASTER')")
    @PatchMapping("/{storeId}/{couponId}")
    public ResponseEntity<Void> updateCoupon(@PathVariable UUID storeId,
        @PathVariable UUID couponId,
        @RequestBody CouponRequestDto dto,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        couponService.updateCoupon(storeId, couponId, dto, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('OWNER', 'MASTER')")
    @DeleteMapping("/{storeId}/{couponId}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable UUID storeId,
        @PathVariable UUID couponId,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        couponService.deleteCoupon(storeId, couponId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }


}
