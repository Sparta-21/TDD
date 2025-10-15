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
import com.sparta.tdd.global.exception.BusinessException;
import com.sparta.tdd.global.exception.ErrorCode;
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
    @Transactional
    public CartResponseDto getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return CartResponseDto.from(cart);
    }

    // 장바구니에 아이템 추가
    @Transactional
    public CartResponseDto addItemToCart(Long userId, CartItemRequestDto request) {
        Cart cart = getOrCreateCart(userId);
        Menu menu = getMenuById(request.menuId());

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

        // 마지막 아이템 삭제 시 가게 정보도 초기화
        cart.checkAndClearStoreIfEmpty();

        return CartResponseDto.from(cart);
    }

    // 장바구니 전체 비우기
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getCartByUserId(userId);
        // store도 함께 null로 설정됨
        cart.clearCart();
    }

    // 기존 아이템이 있으면 수량 증가, 없으면 새로 추가
    private void addOrUpdateCartItem(Cart cart, Menu menu, CartItemRequestDto request) {
        CartItem existingItem = findExistingCartItem(cart, menu);

        if (existingItem != null) {
            // 기존 아이템의 수량 증가
            existingItem.updateQuantity(existingItem.getQuantity() + request.quantity());
            return;
        }

        // 새로운 아이템 추가 (Cart의 addCartItem이 가게 제약 검증함)
        CartItem newCartItem = CartItem.of(menu, request);
        cart.addCartItem(newCartItem, menu.getStore());
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
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_OWNED);
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
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));
    }

    private CartItem getCartItemById(UUID cartItemId) {
        return cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));
    }

    private Menu getMenuById(UUID menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}