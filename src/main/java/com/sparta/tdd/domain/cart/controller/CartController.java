package com.sparta.tdd.domain.cart.controller;

import com.sparta.tdd.domain.auth.UserDetailsImpl;
import com.sparta.tdd.domain.cart.dto.request.CartItemRequestDto;
import com.sparta.tdd.domain.cart.dto.response.CartResponseDto;
import com.sparta.tdd.domain.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/cart")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CUSTOMER','MANAGER','MASTER')")
public class CartController {

    private final CartService cartService;

    // 장바구니 조회
    @GetMapping
    public ResponseEntity<CartResponseDto> getCart(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        CartResponseDto response = cartService.getCart(userDetails.getUserId());
        return ResponseEntity.ok(response);
    }

    // 장바구니에 아이템 추가
    @PostMapping("/items")
    public ResponseEntity<CartResponseDto> addItemToCart(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody CartItemRequestDto request
    ) {
        CartResponseDto response = cartService.addItemToCart(userDetails.getUserId(), request);
        return ResponseEntity.ok(response);
    }

    // 장바구니 아이템 수량 수정
    @PatchMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponseDto> updateCartItemQuantity(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID cartItemId,
            @RequestParam Integer quantity
    ) {
        CartResponseDto response = cartService.updateCartItemQuantity(
                userDetails.getUserId(), cartItemId, quantity
        );
        return ResponseEntity.ok(response);
    }

    // 장바구니 아이템 삭제
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponseDto> removeCartItem(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID cartItemId
    ) {
        CartResponseDto response = cartService.removeCartItem(
                userDetails.getUserId(), cartItemId
        );
        return ResponseEntity.ok(response);
    }

    // 장바구니 전체 비우기
    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        cartService.clearCart(userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }
}