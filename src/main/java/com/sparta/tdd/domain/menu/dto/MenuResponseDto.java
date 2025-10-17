package com.sparta.tdd.domain.menu.dto;

import com.sparta.tdd.domain.menu.entity.Menu;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Builder;

@Schema(description = "메뉴 응답 DTO")
@Builder
public record MenuResponseDto(
    @Schema(description = "menu 아이디", example = "1")
    UUID menuId,
    @Schema(description = "메뉴명", example = "햄버거")
    String name,
    @Schema(description = "메뉴 설명", example = "바삭한 치킨과 신선한 야채가 들어간 버거")
    String description,
    @Schema(description = "가격", example = "10000")
    Integer price,
    @Schema(description = "메뉴 이미지 URL", example = "https://example.com/images/chicken-burger.jpg")
    String imageUrl,
    @Schema(description = "숨김 여부", example = "Boolean.False")
    Boolean isHidden
) {

    public static MenuResponseDto from(Menu menu) {
        return MenuResponseDto.builder()
            .menuId(menu.getId())
            .name(menu.getName())
            .description(menu.getDescription())
            .price(menu.getPrice())
            .imageUrl(menu.getImageUrl())
            .isHidden(menu.getIsHidden()).build();
    }
}
