package com.sparta.tdd.domain.coupon.dto;

import com.sparta.tdd.domain.coupon.entity.UserCoupon;
import com.sparta.tdd.domain.coupon.enums.CouponStatus;
import java.util.UUID;
import lombok.Builder;

@Builder
public record UserCouponResponseDto(
    UUID userCouponId,
    Long userId,
    UUID couponId,
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
