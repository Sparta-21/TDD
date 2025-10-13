package com.sparta.tdd.domain.store.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StoreCategory {
    KOREAN("한식"),
    CHINESE("중식"),
    JAPANESE("일식"),
    WESTERN("양식"),
    FAST_FOOD("패스트푸드"),
    CAFE("카페/디저트"),
    CHICKEN("치킨집"),
    PIZZA("피자"),
    SEAFOOD("해산물"),
    BAKERY("베이커리"),
    STREET_FOOD("분식"),
    VEGAN("비건/건강식");

    private final String description;
}
