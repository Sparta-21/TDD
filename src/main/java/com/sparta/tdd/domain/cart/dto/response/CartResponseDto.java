package com.sparta.tdd.domain.cart.dto.response;

import com.sparta.tdd.domain.cart.entity.Cart;
import java.util.List;
import java.util.UUID;

public record CartResponseDto(
        UUID cartId,
        Long userId,
        List<CartItemResponseDto> items,
        Integer totalPrice,
        UUID storeId // 장바구니는 한 가게의 메뉴만 담을 수 있음
) {
    public static CartResponseDto from(Cart cart) {
        List<CartItemResponseDto> items = cart.getCartItems().stream()
                .map(CartItemResponseDto::from)
                .toList();

        Integer totalPrice = items.stream()
                .mapToInt(CartItemResponseDto::totalPrice)
                .sum();

        UUID storeId = items.isEmpty() ? null : items.get(0).storeId();

        return new CartResponseDto(
                cart.getId(),
                cart.getUser().getId(),
                items,
                totalPrice,
                storeId
        );
    }
}