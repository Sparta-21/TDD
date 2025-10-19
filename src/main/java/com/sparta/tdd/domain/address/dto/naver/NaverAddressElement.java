package com.sparta.tdd.domain.address.dto.naver;

import java.util.List;

public record NaverAddressElement(
        List<String> types,
        String longName,
        String shortName,
        String code
) {
}
