package com.sparta.tdd.domain.payment.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentNumberGenerator {

    // Payment Number 의 경우 "TDD-PAY-{yyyyMMdd}-{임의10글자}" 와 같은 형태로 구성
    private static final String PAYMENT_NUMBER_PREFIX = "TDD-PAY-";
    private static final DateTimeFormatter PAYMENT_FORMATTED_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static String generate() {
        return PAYMENT_NUMBER_PREFIX
            + LocalDateTime.now().format(PAYMENT_FORMATTED_DATE)
            + "-"
            + UUID.randomUUID().toString()
            .substring(0, 9);
    }
}
