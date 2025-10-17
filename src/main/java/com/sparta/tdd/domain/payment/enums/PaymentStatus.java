package com.sparta.tdd.domain.payment.enums;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    PENDING("결제 대기", List.of()),
    COMPLETED("결제 완료", List.of(PENDING)),
    CANCELLED("결제 취소", List.of(PENDING, COMPLETED)),
    FAILED("결제 실패", List.of(PENDING));

    private final String description;
    private final List<PaymentStatus> allowedPreviousStatuses;

    public boolean canTransitionFrom(PaymentStatus currentStatus) {
        return allowedPreviousStatuses.contains(currentStatus);
    }
}
