package com.sparta.tdd.domain.store.dto;

import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.store.enums.StoreCategory;
import com.sparta.tdd.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "가게 등록 요청DTO")
public record StoreRequestDto(

    @NotBlank(message = "가게 이름은 필수입니다.")
    @Schema(description = "가게 이름", example = "홍콩반점")
    String name,

    @NotNull(message = "상점 카테고리는 필수입니다.")
    @Schema(description = "상점 카테고리", implementation = StoreCategory.class)
    StoreCategory category,

    @Size(max = 255)
    @Schema(description = "가게 설명 (최대 255자)", example = "정통 중화요리를 판매하는 홍콩반점입니다.")
    String description,

    @Pattern(regexp = "^(http|https)://.*$")
    @Schema(description = "상점 이미지 URL", example = "https://example.com/image.jpg")
    String imageUrl
) {

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
