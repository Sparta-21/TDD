package com.sparta.tdd.domain.coupon.dto;

import com.sparta.tdd.domain.coupon.entity.Coupon;
import com.sparta.tdd.domain.coupon.enums.Scope;
import com.sparta.tdd.domain.coupon.enums.Type;
import com.sparta.tdd.domain.store.entity.Store;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Builder;

@Schema(description = "쿠폰 요청 DTO")
@Builder
public record CouponRequestDto(
    @Schema(description = "쿠폰 이름", example = "2000원 할인 쿠폰")
    @NotNull @Size(max = 20) String name,
    @Schema(description = "할인 종류", example = "FIXED")
    @NotNull Type type,
    @Schema(description = "사용 범위", example = "STORE")
    @NotNull Scope scope,
    @Schema(description = "할인값", example = "2000")
    @NotNull Integer discountValue,
    @Schema(description = "최소 주문 금액", example = "15000")
    @NotNull Integer minOrderPrice,
    @Schema(description = "발행 개수", example = "100")
    int quantity,
    @Schema(description = "만료일자", example = "2025-09-29T12:00:00.000000000")
    @NotNull LocalDateTime expiredAt
) {

    public Coupon toEntity(Store store) {
        return Coupon.builder()
            .dto(this)
            .store(store)
            .build();
    }

    public boolean masterScope() {
        return scope() == Scope.MASTER;
    }

    public boolean storeScope() {
        return scope() == Scope.STORE;
    }
}
