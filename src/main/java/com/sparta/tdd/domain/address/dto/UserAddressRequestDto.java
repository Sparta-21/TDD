package com.sparta.tdd.domain.address.dto;

import com.sparta.tdd.domain.address.entity.UserAddress;
import com.sparta.tdd.domain.user.entity.User;

public record UserAddressRequestDto(
        String roadAddress,
        String jibunAddress,
        String detailAddress,
        String alias,
        String latitude,
        String longitude
) {
    public static UserAddress toEntity(UserAddressRequestDto userAddressRequestDto, User user) {
        return new UserAddress(
                userAddressRequestDto.jibunAddress(),
                userAddressRequestDto.roadAddress(),
                userAddressRequestDto.detailAddress(),
                userAddressRequestDto.alias(),
                Double.valueOf(userAddressRequestDto.latitude()),
                Double.valueOf(userAddressRequestDto.longitude()),
                user
        );
    }
}
