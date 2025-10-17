package com.sparta.tdd.domain.payment.enums;

import com.sparta.tdd.global.exception.BusinessException;
import com.sparta.tdd.global.exception.ErrorCode;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CardCompany {
    SHINHAN("신한카드"),
    SAMSUNG("삼성카드"),
    KB("국민카드"),
    HYUNDAI("현대카드"),
    LOTTE("롯데카드"),
    WOORI("우리카드"),
    HANA("하나카드");

    private final String description;

    public static CardCompany findByName(String name) {
        return Arrays.stream(CardCompany.values())
            .filter(cardCompany -> cardCompany.name().equalsIgnoreCase(name))
            .findFirst()
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CARD_COMPANY));
    }
}
