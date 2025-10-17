package com.sparta.tdd.domain.address.dto.naver;

import com.sparta.tdd.domain.address.dto.naver.NaverAddressElement;

import java.util.List;

public record NaverAddress(
        String roadAddress,
        String jibunAddress,
        String englishAddress,
        List<NaverAddressElement> addressElements,
        String x,
        String y,
        Double distance
) {
}
