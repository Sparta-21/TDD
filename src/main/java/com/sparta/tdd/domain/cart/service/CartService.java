package com.sparta.tdd.domain.cart.service;

import com.sparta.tdd.domain.cart.dto.request.CartItemRequestDto;
import com.sparta.tdd.domain.cart.dto.response.CartResponseDto;
import com.sparta.tdd.domain.cart.entity.Cart;
import com.sparta.tdd.domain.cart.entity.CartItem;
import com.sparta.tdd.domain.cart.repository.CartItemRepository;
import com.sparta.tdd.domain.cart.repository.CartRepository;
import com.sparta.tdd.domain.menu.entity.Menu;
import com.sparta.tdd.domain.menu.repository.MenuRepository;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuRepository menuRepository;
    private final UserRepository userRepository;

    // 장바구니 조회
    public CartResponseDto getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return CartResponseDto.from(cart);
    }

    // 장바구니에 아이템 추가
    @Transactional
    public CartResponseDto addItemToCart(Long userId, CartItemRequestDto request) {
        Cart cart = getOrCreateCart(userId);
        Menu menu = getMenuById(request.menuId());

        // 장바구니에 이미 다른 가게의 메뉴가 있는지 확인
        validateSameStore(cart, menu);

        // 기존 아이템이 있으면 수량 증가, 없으면 새로 추가
        addOrUpdateCartItem(cart, menu, request);

        return CartResponseDto.from(cart);
    }

    // 장바구니 아이템 수량 수정
    @Transactional
    public CartResponseDto updateCartItemQuantity(Long userId, UUID cartItemId, Integer quantity) {
        Cart cart = getCartByUserId(userId);
        CartItem cartItem = getCartItemById(cartItemId);

        // 카트 소유권 확인
        validateCartOwnership(cart, cartItem);

        cartItem.updateQuantity(quantity);
        return CartResponseDto.from(cart);
    }

    // 장바구니 아이템 삭제
    @Transactional
    public CartResponseDto removeCartItem(Long userId, UUID cartItemId) {
        Cart cart = getCartByUserId(userId);
        CartItem cartItem = getCartItemById(cartItemId);

        validateCartOwnership(cart, cartItem);

        cartItem.delete(userId);
        return CartResponseDto.from(cart);
    }

    // 장바구니 전체 비우기
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getCartByUserId(userId);
        cart.clearCart();
    }

    // 기존 아이템이 있으면 수량 증가, 없으면 새로 추가
    private void addOrUpdateCartItem(Cart cart, Menu menu, CartItemRequestDto request) {
        CartItem existingItem = findExistingCartItem(cart, menu);

        if (existingItem != null) {
            // 기존 아이템의 수량 증가
            existingItem.updateQuantity(existingItem.getQuantity() + request.quantity());
        } else {
            // 새로운 아이템 추가
            CartItem newCartItem = CartItem.of(menu, request);
            cart.addCartItem(newCartItem);
        }
    }

    // 장바구니에서 해당 메뉴의 기존 아이템 찾기
    private CartItem findExistingCartItem(Cart cart, Menu menu) {
        return cartItemRepository
                .findByCartIdAndMenuId(cart.getId(), menu.getId())
                .orElse(null);
    }

    // 장바구니 소유권 검증
    private void validateCartOwnership(Cart cart, CartItem cartItem) {
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("본인의 장바구니 아이템만 수정할 수 있습니다.");
        }
    }

    // 장바구니에 다른 가게의 메뉴가 있는지 검증
    private void validateSameStore(Cart cart, Menu menu) {
        if (cart.getCartItems().isEmpty()) {
            return;
        }

        UUID existingStoreId = cart.getCartItems().get(0).getStore().getId();
        UUID newStoreId = menu.getStore().getId();

        if (!existingStoreId.equals(newStoreId)) {
            throw new IllegalArgumentException(
                    "장바구니에는 한 가게의 메뉴만 담을 수 있습니다. 기존 장바구니를 비우고 다시 시도해주세요."
            );
        }
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> {
                    User user = getUserById(userId);
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    private Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니를 찾을 수 없습니다."));
    }

    private CartItem getCartItemById(UUID cartItemId) {
        return cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 아이템을 찾을 수 없습니다."));
    }

    private Menu getMenuById(UUID menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다."));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}