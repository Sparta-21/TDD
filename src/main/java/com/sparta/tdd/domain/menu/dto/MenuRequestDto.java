package com.sparta.tdd.domain.menu.dto;

import com.sparta.tdd.domain.menu.entity.Menu;
import com.sparta.tdd.domain.store.entity.Store;
import lombok.Builder;

@Builder
public record MenuRequestDto(
    String name,
    String description,
    Integer price,
    String imageUrl
) {

    public Menu toEntity(Store store) {
        return Menu.builder()
            .dto(this)
            .store(store).build();
    }
}
