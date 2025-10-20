package com.sparta.tdd.domain.coupon.dto;

import com.sparta.tdd.domain.coupon.entity.UserCoupon;
import com.sparta.tdd.domain.coupon.enums.CouponStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Builder;

@Schema(description = "유저쿠폰 응답 DTO")
@Builder
public record UserCouponResponseDto(
    @Schema(description = "유저쿠폰 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID userCouponId,
    @Schema(description = "유저 ID", example = "1")
    Long userId,
    @Schema(description = "쿠폰 ID", example = "551e8400-e29b-41d4-a716-446655440001")
    UUID couponId,
    @Schema(description = "유저 쿠폰 상태", example = "ACTIVE")
    CouponStatus couponStatus
) {

    public static UserCouponResponseDto from(UserCoupon userCoupon) {
        return UserCouponResponseDto.builder()
            .userCouponId(userCoupon.getId())
            .userId(userCoupon.getUser().getId())
            .couponId(userCoupon.getCoupon().getId())
            .couponStatus(userCoupon.getCouponStatus())
            .build();
    }
}
