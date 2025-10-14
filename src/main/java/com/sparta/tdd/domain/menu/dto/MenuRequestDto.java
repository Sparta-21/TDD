package com.sparta.tdd.domain.menu.dto;

import com.sparta.tdd.domain.menu.entity.Menu;
import com.sparta.tdd.domain.store.entity.Store;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record MenuRequestDto(
    @NotNull @Size(max = 20) String name,
    String description,
    @NotNull Integer price,
    String imageUrl
) {

    public Menu toEntity(Store store) {
        return Menu.builder()
            .dto(this)
            .store(store).build();
    }
}
