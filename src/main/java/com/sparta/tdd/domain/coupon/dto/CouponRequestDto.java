package com.sparta.tdd.domain.coupon.dto;

import com.sparta.tdd.domain.coupon.entity.Coupon;
import com.sparta.tdd.domain.coupon.enums.Scope;
import com.sparta.tdd.domain.coupon.enums.Type;
import com.sparta.tdd.domain.store.entity.Store;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record CouponRequestDto(
    @NotNull @Size(max = 20) String name,
    @NotNull Type type,
    @NotNull Scope scope,
    @NotNull Integer discountValue,
    @NotNull Integer minOrderPrice,
    int quantity,
    @NotNull LocalDateTime expiredAt
) {

    public Coupon toEntity(Store store) {
        return Coupon.builder()
            .dto(this)
            .store(store)
            .build();
    }
}
