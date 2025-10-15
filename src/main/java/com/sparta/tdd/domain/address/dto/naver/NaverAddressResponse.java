package com.sparta.tdd.domain.address.dto.naver;

import java.util.List;

public record NaverAddressResponse(
        String status,
        Meta meta,
        List<NaverAddress> addresses,
        String errorMessage
) {
}
