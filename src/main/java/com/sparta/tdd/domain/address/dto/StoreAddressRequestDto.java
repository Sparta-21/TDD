package com.sparta.tdd.domain.address.dto;

import com.sparta.tdd.domain.address.entity.StoreAddress;
import com.sparta.tdd.domain.store.entity.Store;

public record StoreAddressRequestDto(
        String roadAddress,
        String jibunAddress,
        String detailAddress,
        String latitude,
        String longitude
) {
    public static StoreAddress toEntity(StoreAddressRequestDto requestDto, Store store) {
        return new StoreAddress(
                requestDto.jibunAddress(),
                requestDto.roadAddress(),
                requestDto.detailAddress(),
                Double.valueOf(requestDto.latitude()),
                Double.valueOf(requestDto.longitude()),
                store
        );
    }
}
