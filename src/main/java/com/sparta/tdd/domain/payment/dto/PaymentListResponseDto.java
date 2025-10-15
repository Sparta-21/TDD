package com.sparta.tdd.domain.payment.dto;

import com.sparta.tdd.domain.payment.entity.Payment;
import java.util.UUID;
import org.springframework.data.domain.Page;

public record PaymentListResponseDto(
    UUID id,
    String paymentNumber,
    String status,
    String storeName,
    Long price
) {

    public static Page<PaymentListResponseDto> from(Page<Payment> paymentPage) {
        return paymentPage.map(payment -> new PaymentListResponseDto(
            payment.getId(),
            payment.getNumber(),
            payment.getStatus().getDescription(),
            payment.getOrder() != null && payment.getOrder().getStore() != null
                ? payment.getOrder().getStore().getName()
                : "",
            payment.getAmount()
        ));
    }
}
