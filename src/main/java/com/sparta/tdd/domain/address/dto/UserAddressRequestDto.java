package com.sparta.tdd.domain.address.dto;

public record AddressRequestDto(
        String roadAddress,
        String jibunAddress,
        String detailAddress,
        String latitude,
        String longitude
) {
}
