package com.sparta.tdd.domain.address.dto;

import com.sparta.tdd.domain.address.entity.UserAddress;

import java.util.UUID;

public record UserAddressResponseDto(
        UUID uuid,
        String jibunAddress,
        String roadAddress,
        String detailAddress,
        String alias,
        String latitude,
        String longitude
) {
    public static UserAddressResponseDto from(UserAddress userAddress) {
        return new UserAddressResponseDto(
                userAddress.getId(),
                userAddress.getJibunAddress(),
                userAddress.getRoadAddress(),
                userAddress.getDetailAddress(),
                userAddress.getAlias(),
                String.valueOf(userAddress.getLatitude()),
                String.valueOf(userAddress.getLongitude())
        );
    }
}
