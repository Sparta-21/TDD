package com.sparta.tdd.domain.menu.dto;

import com.sparta.tdd.domain.menu.entity.Menu;
import com.sparta.tdd.domain.store.entity.Store;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Schema(description = "메뉴 등록 및 수정 요청 DTO")
@Builder
public record MenuRequestDto(
    @Schema(description = "메뉴 설명", example = "바삭한 치킨과 신선한 야채가 들어간 버거")
    @NotNull @Size(max = 20) String name,
    String description,
    @Schema(description = "가격", example = "10000")
    @NotNull Integer price,
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
