package com.sparta.tdd.domain.coupon.controller;

import com.sparta.tdd.domain.auth.UserDetailsImpl;
import com.sparta.tdd.domain.coupon.dto.CouponRequestDto;
import com.sparta.tdd.domain.coupon.dto.CouponResponseDto;
import com.sparta.tdd.domain.coupon.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(
        summary = "가게 쿠폰 목록 조회",
        description = "해당 가게의 모든 쿠폰을 조회합니다."
    )
    @GetMapping("/list/{storeId}")
    public ResponseEntity<List<CouponResponseDto>> getStoreCoupons(@PathVariable UUID storeId) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(couponService.getStoreCoupons(storeId));
    }

    @Operation(
        summary = "STORE 쿠폰 등록",
        description =
            "해당 가게에만 적용할 수 있는 STORE 쿠폰을 발급합니다. OWNER, MASTER 권한만 접근 가능하며, 쿠폰 발급 시 최초 쿠폰 SCOPE는 STORE입니다."
                + "TYPE 에서 FIXED(고정된 할인값), PERCENT(할인율)을 선택할 수 있으며 사용가능한 최소 금액과 만료기간을 필수로 입력해야 합니다."
    )
    @PreAuthorize("hasAnyRole('OWNER', 'MASTER')")
    @PostMapping("/{storeId}")
    public ResponseEntity<CouponResponseDto> createStoreCoupon(@PathVariable UUID storeId,
        @Valid @RequestBody CouponRequestDto dto,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(couponService.createStoreCoupon(storeId, dto, userDetails.getUserId()));
    }

    @Operation(
        summary = "MASTER 쿠폰 등록",
        description =
            "가게와 무관하게 적용할 수 있는 MASTER 쿠폰을 발급합니다. MASTER 권한만 접근 가능하며, 쿠폰 발급 시 최초 쿠폰 SCOPE는 MASTER입니다."
                + "TYPE 에서 FIXED(고정된 할인값), PERCENT(할인율)을 선택할 수 있으며 사용가능한 최소 금액과 만료기간을 필수로 입력해야 합니다."
    )
    @PreAuthorize("hasAnyRole('MASTER')")
    @PostMapping("/master")
    public ResponseEntity<CouponResponseDto> createMasterCoupon(
        @Valid @RequestBody CouponRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(couponService.createMasterCoupon(dto));
    }

    @Operation(
        summary = "쿠폰 수정",
        description = "쿠폰을 수정합니다. 해당 가게 점주(OWNER), MASTER 권한만 가능하며 이미 1회라도 사용자가 발급했다면 수정할 수 없습니다."
    )
    @PreAuthorize("hasAnyRole('OWNER', 'MASTER')")
    @PatchMapping("/{storeId}/{couponId}")
    public ResponseEntity<Void> updateCoupon(@PathVariable UUID storeId,
        @PathVariable UUID couponId,
        @RequestBody CouponRequestDto dto,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        couponService.updateCoupon(storeId, couponId, dto, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "쿠폰 삭제",
        description =
            "쿠폰을 삭제합니다(sort delete). 해당 가게 점주(OWNER), MASTER 권한만 가능하며 해당 쿠폰이 삭제되어도(추가 발급 불가)"
                + " 이미 발급된 유저쿠폰은 적용할 수 있습니다."
    )
    @PreAuthorize("hasAnyRole('OWNER', 'MASTER')")
    @DeleteMapping("/{storeId}/{couponId}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable UUID storeId,
        @PathVariable UUID couponId,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        couponService.deleteCoupon(storeId, couponId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }


}
