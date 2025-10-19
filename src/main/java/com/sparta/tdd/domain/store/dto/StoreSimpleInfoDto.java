package com.sparta.tdd.domain.store.dto;

import com.sparta.tdd.domain.store.entity.Store;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record StoreSimpleInfoDto(

    @Schema(description = "가게 ID", example = "4f1ed1a0-e7dc-4f7d-a806-412e0e07bfbe")
    UUID id,

    @Schema(description = "가게 이름", example = "홍콩반점")
    String storeName
) {

    public static StoreSimpleInfoDto from(Store store) {
        return new StoreSimpleInfoDto(
            store.getId(),
            store.getName()
        );
    }
}
