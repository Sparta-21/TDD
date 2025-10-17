package com.sparta.tdd.domain.address.dto;

import com.sparta.tdd.domain.address.entity.StoreAddress;

import java.util.UUID;

public record StoreAddressResponseDto(
        UUID uuid,
        String jibunAddress,
        String roadAddress,
        String detailAddress,
        String latitude,
        String longitude
) {
    public static StoreAddressResponseDto from(StoreAddress storeAddress) {
        return new StoreAddressResponseDto(
                storeAddress.getId(),
                storeAddress.getJibunAddress(),
                storeAddress.getRoadAddress(),
                storeAddress.getDetailAddress(),
                String.valueOf(storeAddress.getLatitude()),
                String.valueOf(storeAddress.getLongitude())
        );
    }
}
