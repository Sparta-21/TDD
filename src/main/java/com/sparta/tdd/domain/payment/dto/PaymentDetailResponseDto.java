package com.sparta.tdd.domain.payment.dto;

import com.sparta.tdd.domain.order.dto.OrderItemInfoDto;
import com.sparta.tdd.domain.payment.entity.Payment;
import com.sparta.tdd.domain.store.dto.StoreSimpleInfoDto;
import java.time.LocalDateTime;
import java.util.List;

public record PaymentDetailResponseDto(
    String paymentNumber,
    Long price,
    String cardCompany,
    String cardNumber,
    LocalDateTime processedAt,
    StoreSimpleInfoDto restaurant,
    List<OrderItemInfoDto> orderItem
) {

    public static PaymentDetailResponseDto from(Payment payment) {
        StoreSimpleInfoDto restaurantInfo = StoreSimpleInfoDto.from(payment.getOrder().getStore());
        List<OrderItemInfoDto> orderItems = OrderItemInfoDto.fromList(payment.getOrder().getOrderMenuList());

        return new PaymentDetailResponseDto(
            payment.getNumber(),
            payment.getAmount(),
            payment.getCardCompany().getDescription(),
            maskCardNumber(payment.getCardNumber()),
            payment.getProcessedAt(),
            restaurantInfo,
            orderItems
        );
    }

    private static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) {
            return cardNumber;
        }

        int length = cardNumber.length();
        String firstFour = cardNumber.substring(0, 4);
        String lastFour = cardNumber.substring(length - 4);
        int middleLength = length - 8;

        StringBuilder masked = new StringBuilder();
        for (int i = 0; i < middleLength; i++) {
            if (i > 0 && i % 4 == 0) {
                masked.append(" ");
            }
            masked.append("*");
        }

        return firstFour + " " + masked + " " + lastFour;
    }
}
