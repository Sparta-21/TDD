package com.sparta.tdd.domain.cart.dto.response;

import com.sparta.tdd.domain.cart.entity.Cart;
import com.sparta.tdd.domain.store.entity.Store;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;
@Schema(description = "장바구니 응답 DTO")
public record CartResponseDto(
        @Schema(description = "장바구니 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID cartId,

        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "장바구니 아이템 목록")
        List<CartItemResponseDto> items,

        @Schema(description = "총 금액", example = "35000")
        Integer totalPrice,

        @Schema(description = "가게 ID (아이템이 없으면 null)", example = "550e8400-e29b-41d4-a716-446655440001")
        UUID storeId,

        @Schema(description = "가게 이름 (아이템이 없으면 null)", example = "맛있는 치킨집")
        String storeName
) {
    public static CartResponseDto from(Cart cart) {
        List<CartItemResponseDto> items = cart.getCartItems().stream()
                .filter(item -> !item.isDeleted())
                .map(CartItemResponseDto::from)
                .toList();

        Integer totalPrice = items.stream()
                .mapToInt(CartItemResponseDto::totalPrice)
                .sum();

        Store store = cart.getStore();
        UUID storeId = (store != null) ? store.getId() : null;
        String storeName = (store != null) ? store.getName() : null;

        return new CartResponseDto(
                cart.getId(),
                cart.getUser().getId(),
                items,
                totalPrice,
                storeId,
                storeName
        );
    }
}