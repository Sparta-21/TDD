package com.sparta.tdd.domain.payment.dto;

import com.sparta.tdd.domain.payment.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdatePaymentStatusRequest(
    @NotNull(message = "결제 상태는 필수입니다.(COMPLETED, CANCELLED, FAILED)")
    PaymentStatus status
) {

}