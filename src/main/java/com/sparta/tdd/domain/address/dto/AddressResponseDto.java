package com.sparta.tdd.domain.address.dto;

import com.sparta.tdd.domain.address.dto.naver.NaverAddress;

public record AddressResponseDto(
        String jibunAddress,
        String roadAddress,
        String latitude,
        String longitude
) {
    public static AddressResponseDto from(NaverAddress address) {
        return new AddressResponseDto(
                address.jibunAddress(),
                address.roadAddress(),
                address.x(),
                address.y()
        );
    }
}
