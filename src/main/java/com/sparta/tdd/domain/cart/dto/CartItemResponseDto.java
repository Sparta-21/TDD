package com.sparta.tdd.domain.cart.dto;

import com.sparta.tdd.domain.cart.entity.CartItem;
import java.util.UUID;

public record CartItemResponseDto(
        UUID cartItemId,
        UUID menuId,
        String menuName,
        Integer price,
        Integer quantity,
        Integer totalPrice,
        UUID storeId,
        String storeName
) {
    public static CartItemResponseDto from(CartItem cartItem) {
        return new CartItemResponseDto(
                cartItem.getId(),
                cartItem.getMenu().getId(),
                cartItem.getMenu().getName(),
                cartItem.getPrice(),
                cartItem.getQuantity(),
                cartItem.getPrice() * cartItem.getQuantity(),
                cartItem.getStore().getId(),
                cartItem.getStore().getName()
        );
    }
}