package com.sparta.tdd.domain.cart.dto.response;

import com.sparta.tdd.domain.cart.entity.CartItem;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "장바구니 아이템 정보")
public record CartItemResponseDto(
        @Schema(description = "장바구니 아이템 ID", example = "550e8400-e29b-41d4-a716-446655440002")
        UUID cartItemId,

        @Schema(description = "메뉴 ID", example = "550e8400-e29b-41d4-a716-446655440003")
        UUID menuId,

        @Schema(description = "메뉴 이름", example = "후라이드 치킨")
        String menuName,

        @Schema(description = "메뉴 가격", example = "17000")
        Integer price,

        @Schema(description = "주문 수량", example = "2")
        Integer quantity,

        @Schema(description = "총액 (가격 × 수량)", example = "34000")
        Integer totalPrice

) {
    public static CartItemResponseDto from(CartItem cartItem) {
        return new CartItemResponseDto(
                cartItem.getId(),
                cartItem.getMenu().getId(),
                cartItem.getMenu().getName(),
                cartItem.getPrice(),
                cartItem.getQuantity(),
                cartItem.getPrice() * cartItem.getQuantity()
        );
    }
}