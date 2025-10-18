package com.sparta.tdd.domain.coupon.dto;

import com.sparta.tdd.domain.coupon.entity.Coupon;
import com.sparta.tdd.domain.coupon.enums.Scope;
import com.sparta.tdd.domain.coupon.enums.Type;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Schema(description = "쿠폰 응답 DTO")
@Builder
public record CouponResponseDto(
    @Schema(description = "쿠폰 ID", example = "551e8400-e29b-41d4-a716-446655440001")
    UUID couponId,
    @Schema(description = "쿠폰 이름", example = "2000원 할인 쿠폰")
    String name,
    @Schema(description = "할인 종류", example = "FIXED")
    Type type,
    @Schema(description = "사용 범위", example = "STORE")
    Scope scope,
    @Schema(description = "할인값", example = "2000")
    Integer discountValue,
    @Schema(description = "최소 주문 금액", example = "15000")
    Integer minOrderPrice,
    @Schema(description = "발행 개수", example = "100")
    int quantity,
    @Schema(description = "발급된 수량", example = "30")
    int issuedCount,
    @Schema(description = "만료일자", example = "2025-09-29T12:00:00.000000000")
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
