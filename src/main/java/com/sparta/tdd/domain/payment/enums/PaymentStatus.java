package com.sparta.tdd.domain.payment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    PENDING("결제 대기"),
    COMPLETED("결제 완료"),
    CANCELLED("결제 취소"),
    FAILED("결제 실패");

    private final String description;
}
