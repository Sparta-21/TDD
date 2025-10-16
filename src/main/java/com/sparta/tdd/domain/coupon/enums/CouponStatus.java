package com.sparta.tdd.domain.coupon.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CouponStatus {
    ACTIVE, USED, EXPIRED;
}
