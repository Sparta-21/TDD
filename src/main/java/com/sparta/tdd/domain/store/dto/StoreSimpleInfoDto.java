package com.sparta.tdd.domain.store.dto;

import com.sparta.tdd.domain.store.entity.Store;
import java.util.UUID;

public record StoreSimpleInfoDto(
    UUID id,
    String storeName
) {

    public static StoreSimpleInfoDto from(Store store) {
        return new StoreSimpleInfoDto(
            store.getId(),
            store.getName()
        );
    }
}
