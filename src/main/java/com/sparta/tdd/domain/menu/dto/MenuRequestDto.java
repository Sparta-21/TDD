package com.sparta.tdd.domain.menu.dto;

import com.sparta.tdd.domain.menu.entity.Menu;
import com.sparta.tdd.domain.store.entity.Store;
import lombok.Builder;

@Builder
public record MenuRequestDto(
    String name,
    String description,
    Integer price,
    String imageUrl,
    boolean useAiDescription
) {

    public Menu toEntity(Store store, String description) {
        return Menu.builder()
            .name(name)
            .description(description)
            .price(price)
            .imageUrl(imageUrl)
            .store(store)
            .build();
    }
    
    public Menu toEntity(Store store) {
        return Menu.builder()
            .name(name)
            .description(description)
            .price(price)
            .imageUrl(imageUrl)
            .store(store)
            .build();
    }
}
