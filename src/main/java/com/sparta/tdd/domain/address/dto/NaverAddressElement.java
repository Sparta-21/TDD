package com.sparta.tdd.domain.address.dto;

import java.util.List;

public record NaverAddressElement(
        List<String> types,
        String longName,
        String shortName,
        String code
) {
}
