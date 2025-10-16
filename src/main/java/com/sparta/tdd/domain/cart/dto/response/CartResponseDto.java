package com.sparta.tdd.domain.cart.dto.response;

import com.sparta.tdd.domain.cart.entity.Cart;
import com.sparta.tdd.domain.store.entity.Store;

import java.util.List;
import java.util.UUID;

public record CartResponseDto(
        UUID cartId,
        Long userId,
        List<CartItemResponseDto> items,
        Integer totalPrice,
        UUID storeId,
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