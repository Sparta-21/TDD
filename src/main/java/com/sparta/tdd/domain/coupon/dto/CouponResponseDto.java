package com.sparta.tdd.domain.coupon.dto;

import com.sparta.tdd.domain.coupon.entity.Coupon;
import com.sparta.tdd.domain.coupon.enums.Scope;
import com.sparta.tdd.domain.coupon.enums.Type;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CouponResponseDto(
    UUID couponId,
    String name,
    Type type,
    Scope scope,
    Integer discountValue,
    Integer minOrderPrice,
    int quantity,
    int issuedCount,
    LocalDateTime expiredAt
) {

    public static CouponResponseDto from(Coupon coupon) {
        return CouponResponseDto.builder()
            .couponId(coupon.getId())
            .name(coupon.getName())
            .type(coupon.getType())
            .scope(coupon.getScope())
            .discountValue(coupon.getDiscountValue())
            .minOrderPrice(coupon.getMinOrderPrice())
            .quantity(coupon.getQuantity())
            .issuedCount(coupon.getIssuedCount())
            .expiredAt(coupon.getExpiredAt())
            .build();
    }

}
