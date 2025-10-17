package com.sparta.tdd.domain.point.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PointType {
    PAYMENT_EARNED("결제 적립"),
    REVIEW_EARNED("리뷰 적립"),
    PAYMENT_CANCELLED("결제 취소"),
    USED("사용"),
    EXPIRED("만료");

    private final String description;
}
