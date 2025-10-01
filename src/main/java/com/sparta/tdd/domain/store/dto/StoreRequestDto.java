package com.sparta.tdd.domain.store.dto;

import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.store.enums.StoreCategory;
import com.sparta.tdd.domain.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record StoreRequestDto(

    @NotBlank
    String name,

    @NotNull
    StoreCategory category,

    @Size(max = 255)
    String description,

    @Pattern(regexp = "^(http|https)://.*$")
    String imageUrl) {

    public Store toEntity(User user) {
        return Store.builder()
            .name(name)
            .user(user)
            .description(description)
            .category(category)
            .imageUrl(imageUrl)
            .build();
    }
}
