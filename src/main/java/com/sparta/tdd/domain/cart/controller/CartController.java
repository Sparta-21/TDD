package com.sparta.tdd.domain.cart.controller;

import com.sparta.tdd.domain.auth.UserDetailsImpl;
import com.sparta.tdd.domain.cart.dto.request.CartItemRequestDto;
import com.sparta.tdd.domain.cart.dto.response.CartResponseDto;
import com.sparta.tdd.domain.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Cart", description = "장바구니 관리 API")
@RestController
@RequestMapping("/v1/cart")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CUSTOMER','MANAGER','MASTER')")
public class CartController {

    private final CartService cartService;

    // 장바구니 조회
    @Operation(
            summary = "장바구니 조회",
            description = """
        사용자의 장바구니를 조회합니다.\n
        장바구니가 없으면 자동으로 생성됩니다.\n
        장바구니에 담긴 모든 아이템과 총 금액을 반환합니다.\n
        CUSTOMER, MANAGER, MASTER 권한이 필요합니다.
        """
    )
    @GetMapping
    public ResponseEntity<CartResponseDto> getCart(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        CartResponseDto response = cartService.getCart(userDetails.getUserId());
        return ResponseEntity.ok(response);
    }

    // 장바구니에 아이템 추가
    @Operation(
            summary = "장바구니에 아이템 추가",
            description = """
        장바구니에 메뉴를 추가합니다.\n
        같은 메뉴가 이미 있으면 수량이 증가합니다.\n
        다른 가게의 메뉴를 추가하려면 기존 장바구니를 비워야 합니다.\n
        추가된 장바구니 정보를 반환합니다.
        """
    )
    @PostMapping("/items")
    public ResponseEntity<CartResponseDto> addItemToCart(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody CartItemRequestDto request
    ) {
        CartResponseDto response = cartService.addItemToCart(userDetails.getUserId(), request);
        return ResponseEntity.ok(response);
    }

    // 장바구니 아이템 수량 수정
    @Operation(
            summary = "장바구니 아이템 수량 수정",
            description = """
        장바구니에 담긴 아이템의 수량을 변경합니다.\n
        수량은 1개 이상이어야 합니다.\n
        본인의 장바구니 아이템만 수정할 수 있습니다.\n
        수정된 장바구니 정보를 반환합니다.
        """
    )
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
    @Operation(
            summary = "장바구니 아이템 삭제",
            description = """
        장바구니에서 특정 아이템을 삭제합니다.\n
        본인의 장바구니 아이템만 삭제할 수 있습니다.\n
        마지막 아이템을 삭제하면 가게 정보도 초기화됩니다.\n
        삭제 후 장바구니 정보를 반환합니다.
        """
    )
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
    @Operation(
            summary = "장바구니 전체 비우기",
            description = """
        장바구니의 모든 아이템을 삭제합니다.\n
        가게 정보도 함께 초기화됩니다.\n
        삭제 성공 시 204 No Content 상태로 응답합니다.
        """
    )
    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        cartService.clearCart(userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }
}