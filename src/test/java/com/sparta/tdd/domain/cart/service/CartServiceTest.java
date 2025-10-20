package com.sparta.tdd.domain.cart.service;

import com.sparta.tdd.domain.cart.dto.request.CartItemRequestDto;
import com.sparta.tdd.domain.cart.dto.response.CartResponseDto;
import com.sparta.tdd.domain.cart.entity.Cart;
import com.sparta.tdd.domain.cart.entity.CartItem;
import com.sparta.tdd.domain.cart.repository.CartItemRepository;
import com.sparta.tdd.domain.cart.repository.CartRepository;
import com.sparta.tdd.domain.menu.entity.Menu;
import com.sparta.tdd.domain.menu.repository.MenuRepository;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.store.enums.StoreCategory;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import com.sparta.tdd.domain.user.repository.UserRepository;
import com.sparta.tdd.global.exception.BusinessException;
import com.sparta.tdd.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService 테스트")
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartService cartService;

    private User user;
    private User owner;
    private Store store;
    private Menu menu;
    private Cart cart;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        // 테스트 유저
        user = User.builder()
                .username("testuser")
                .password("Password1!")
                .nickname("테스트유저")
                .authority(UserAuthority.CUSTOMER)
                .build();
        setId(user, 1L);

        // 가게 소유자
        owner = User.builder()
                .username("owner")
                .password("Password1!")
                .nickname("사장님")
                .authority(UserAuthority.OWNER)
                .build();
        setId(owner, 2L);

        // 가게
        store = Store.builder()
                .name("테스트 가게")
                .category(StoreCategory.KOREAN)
                .description("맛있는 한식집")
                .user(owner)
                .build();
        setId(store, UUID.randomUUID());

        // 메뉴
        menu = Menu.builder()
                .name("김치찌개")
                .description("맛있는 김치찌개")
                .price(8000)
                .imageUrl(null)
                .store(store)
                .build();
        setId(menu, UUID.randomUUID());

        // 장바구니
        cart = Cart.builder()
                .user(user)
                .build();
        setId(cart, UUID.randomUUID());

        // 장바구니 아이템
        cartItem = CartItem.builder()
                .menu(menu)
                .quantity(2)
                .price(menu.getPrice())
                .build();
        setId(cartItem, UUID.randomUUID());
    }

    private void setId(User user, Long id) {
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setId(Object entity, UUID id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nested
    @DisplayName("장바구니 조회")
    class GetCartTest {

        @Test
        @DisplayName("기존 장바구니 조회 - 성공")
        void getCart_ExistingCart_Success() {
            // given
            Long userId = 1L;
            given(cartRepository.findByUserIdWithItems(userId))
                    .willReturn(Optional.of(cart));

            // when
            CartResponseDto result = cartService.getCart(userId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.userId()).isEqualTo(user.getId());
            verify(cartRepository).findByUserIdWithItems(userId);
        }

        @Test
        @DisplayName("장바구니가 없으면 새로 생성 - 성공")
        void getCart_CreateNewCart_Success() {
            // given
            Long userId = 1L;
            given(cartRepository.findByUserIdWithItems(userId))
                    .willReturn(Optional.empty());
            given(userRepository.findById(userId))
                    .willReturn(Optional.of(user));
            given(cartRepository.save(any(Cart.class)))
                    .willReturn(cart);

            // when
            CartResponseDto result = cartService.getCart(userId);

            // then
            assertThat(result).isNotNull();
            verify(cartRepository).findByUserIdWithItems(userId);
            verify(userRepository).findById(userId);
            verify(cartRepository).save(any(Cart.class));
        }

        @Test
        @DisplayName("존재하지 않는 사용자 - 실패")
        void getCart_UserNotFound_Fail() {
            // given
            Long userId = 999L;
            given(cartRepository.findByUserIdWithItems(userId))
                    .willReturn(Optional.empty());
            given(userRepository.findById(userId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cartService.getCart(userId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("장바구니 아이템 추가")
    class AddItemToCartTest {

        @Test
        @DisplayName("새 아이템 추가 - 성공")
        void addItemToCart_NewItem_Success() {
            // given
            Long userId = 1L;
            UUID menuId = menu.getId();
            CartItemRequestDto request = new CartItemRequestDto(menuId, 2);

            given(cartRepository.findByUserIdWithItems(userId))
                    .willReturn(Optional.of(cart));
            given(menuRepository.findById(menuId))
                    .willReturn(Optional.of(menu));
            given(cartItemRepository.findByCartIdAndMenuId(any(UUID.class), eq(menuId)))
                    .willReturn(Optional.empty());

            // when
            CartResponseDto result = cartService.addItemToCart(userId, request);

            // then
            assertThat(result).isNotNull();
            verify(cartRepository).findByUserIdWithItems(userId);
            verify(menuRepository).findById(menuId);
            verify(cartItemRepository).findByCartIdAndMenuId(any(UUID.class), eq(menuId));
        }

        @Test
        @DisplayName("기존 아이템 수량 증가 - 성공")
        void addItemToCart_ExistingItem_IncreaseQuantity() {
            // given
            Long userId = 1L;
            UUID menuId = menu.getId();
            CartItemRequestDto request = new CartItemRequestDto(menuId, 3);

            cart.addCartItem(cartItem, store);

            given(cartRepository.findByUserIdWithItems(userId))
                    .willReturn(Optional.of(cart));
            given(menuRepository.findById(menuId))
                    .willReturn(Optional.of(menu));
            given(cartItemRepository.findByCartIdAndMenuId(any(UUID.class), eq(menuId)))
                    .willReturn(Optional.of(cartItem));

            int originalQuantity = cartItem.getQuantity();

            // when
            CartResponseDto result = cartService.addItemToCart(userId, request);

            // then
            assertThat(result).isNotNull();
            assertThat(cartItem.getQuantity()).isEqualTo(originalQuantity + request.quantity());
        }

        @Test
        @DisplayName("다른 가게의 메뉴 추가 시도 - 실패")
        void addItemToCart_DifferentStore_Fail() {
            // given
            Long userId = 1L;
            UUID menuId = UUID.randomUUID();
            CartItemRequestDto request = new CartItemRequestDto(menuId, 2);

            // 다른 가게
            Store differentStore = Store.builder()
                    .name("다른 가게")
                    .category(StoreCategory.CHINESE)
                    .user(owner)
                    .build();
            setId(differentStore, UUID.randomUUID());

            Menu differentMenu = Menu.builder()
                    .name("짜장면")
                    .description("맛있는 짜장면")
                    .price(6000)
                    .imageUrl(null)
                    .store(differentStore)
                    .build();
            setId(differentMenu, menuId);

            cart.addCartItem(cartItem, store);

            given(cartRepository.findByUserIdWithItems(userId))
                    .willReturn(Optional.of(cart));
            given(menuRepository.findById(menuId))
                    .willReturn(Optional.of(differentMenu));

            // when & then
            assertThatThrownBy(() -> cartService.addItemToCart(userId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_DIFFERENT_STORE);
        }

        @Test
        @DisplayName("존재하지 않는 메뉴 - 실패")
        void addItemToCart_MenuNotFound_Fail() {
            // given
            Long userId = 1L;
            UUID menuId = UUID.randomUUID();
            CartItemRequestDto request = new CartItemRequestDto(menuId, 2);

            given(cartRepository.findByUserIdWithItems(userId))
                    .willReturn(Optional.of(cart));
            given(menuRepository.findById(menuId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cartService.addItemToCart(userId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("장바구니 아이템 수량 수정")
    class UpdateCartItemQuantityTest {

        @Test
        @DisplayName("수량 수정 - 성공")
        void updateCartItemQuantity_Success() {
            // given
            Long userId = 1L;
            UUID cartItemId = cartItem.getId();
            Integer newQuantity = 5;

            cart.addCartItem(cartItem, store);

            given(cartRepository.findByUserIdWithItems(userId))
                    .willReturn(Optional.of(cart));
            given(cartItemRepository.findById(cartItemId))
                    .willReturn(Optional.of(cartItem));

            // when
            CartResponseDto result = cartService.updateCartItemQuantity(userId, cartItemId, newQuantity);

            // then
            assertThat(result).isNotNull();
            assertThat(cartItem.getQuantity()).isEqualTo(newQuantity);
        }

        @Test
        @DisplayName("수량을 0 이하로 수정 시도 - 실패")
        void updateCartItemQuantity_InvalidQuantity_Fail() {
            // given
            Long userId = 1L;
            UUID cartItemId = cartItem.getId();
            Integer invalidQuantity = 0;

            cart.addCartItem(cartItem, store);

            given(cartRepository.findByUserIdWithItems(userId))
                    .willReturn(Optional.of(cart));
            given(cartItemRepository.findById(cartItemId))
                    .willReturn(Optional.of(cartItem));

            // when & then
            assertThatThrownBy(() -> cartService.updateCartItemQuantity(userId, cartItemId, invalidQuantity))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_ITEM_INVALID_QUANTITY);
        }

        @Test
        @DisplayName("다른 사용자의 장바구니 아이템 수정 시도 - 실패")
        void updateCartItemQuantity_NotOwned_Fail() {
            // given
            Long userId = 1L;
            UUID cartItemId = cartItem.getId();
            Integer newQuantity = 5;

            Cart otherCart = Cart.builder()
                    .user(owner)
                    .build();
            setId(otherCart, UUID.randomUUID());
            otherCart.addCartItem(cartItem, store);

            given(cartRepository.findByUserIdWithItems(userId))
                    .willReturn(Optional.of(cart));
            given(cartItemRepository.findById(cartItemId))
                    .willReturn(Optional.of(cartItem));

            // when & then
            assertThatThrownBy(() -> cartService.updateCartItemQuantity(userId, cartItemId, newQuantity))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_ITEM_NOT_OWNED);
        }

        @Test
        @DisplayName("존재하지 않는 장바구니 아이템 - 실패")
        void updateCartItemQuantity_ItemNotFound_Fail() {
            // given
            Long userId = 1L;
            UUID cartItemId = UUID.randomUUID();
            Integer newQuantity = 5;

            given(cartRepository.findByUserIdWithItems(userId))
                    .willReturn(Optional.of(cart));
            given(cartItemRepository.findById(cartItemId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cartService.updateCartItemQuantity(userId, cartItemId, newQuantity))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_ITEM_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("장바구니 아이템 삭제")
    class RemoveCartItemTest {

        @Test
        @DisplayName("아이템 삭제 - 성공")
        void removeCartItem_Success() {
            // given
            Long userId = 1L;
            UUID cartItemId = cartItem.getId();

            cart.addCartItem(cartItem, store);

            given(cartRepository.findByUserIdWithItems(userId))
                    .willReturn(Optional.of(cart));
            given(cartItemRepository.findById(cartItemId))
                    .willReturn(Optional.of(cartItem));

            // when
            CartResponseDto result = cartService.removeCartItem(userId, cartItemId);

            // then
            assertThat(result).isNotNull();
            assertThat(cartItem.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("마지막 아이템 삭제 시 가게 정보 초기화 - 성공")
        void removeCartItem_LastItem_ClearStore() {
            // given
            Long userId = 1L;
            UUID cartItemId = cartItem.getId();

            cart.addCartItem(cartItem, store);

            given(cartRepository.findByUserIdWithItems(userId))
                    .willReturn(Optional.of(cart));
            given(cartItemRepository.findById(cartItemId))
                    .willReturn(Optional.of(cartItem));

            // when
            CartResponseDto result = cartService.removeCartItem(userId, cartItemId);

            // then
            assertThat(result).isNotNull();
            verify(cartRepository).findByUserIdWithItems(userId);
        }

        @Test
        @DisplayName("다른 사용자의 장바구니 아이템 삭제 시도 - 실패")
        void removeCartItem_NotOwned_Fail() {
            // given
            Long userId = 1L;
            UUID cartItemId = cartItem.getId();

            Cart otherCart = Cart.builder()
                    .user(owner)
                    .build();
            setId(otherCart, UUID.randomUUID());
            otherCart.addCartItem(cartItem, store);

            given(cartRepository.findByUserIdWithItems(userId))
                    .willReturn(Optional.of(cart));
            given(cartItemRepository.findById(cartItemId))
                    .willReturn(Optional.of(cartItem));

            // when & then
            assertThatThrownBy(() -> cartService.removeCartItem(userId, cartItemId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_ITEM_NOT_OWNED);
        }

        @Test
        @DisplayName("존재하지 않는 장바구니 아이템 - 실패")
        void removeCartItem_ItemNotFound_Fail() {
            // given
            Long userId = 1L;
            UUID cartItemId = UUID.randomUUID();

            given(cartRepository.findByUserIdWithItems(userId))
                    .willReturn(Optional.of(cart));
            given(cartItemRepository.findById(cartItemId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cartService.removeCartItem(userId, cartItemId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_ITEM_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("장바구니 전체 비우기")
    class ClearCartTest {

        @Test
        @DisplayName("장바구니 비우기 - 성공")
        void clearCart_Success() {
            // given
            Long userId = 1L;

            cart.addCartItem(cartItem, store);

            given(cartRepository.findByUserIdWithItems(userId))
                    .willReturn(Optional.of(cart));

            // when
            cartService.clearCart(userId);

            // then
            verify(cartRepository).findByUserIdWithItems(userId);
        }

        @Test
        @DisplayName("장바구니가 없을 때 - 실패")
        void clearCart_CartNotFound_Fail() {
            // given
            Long userId = 1L;

            given(cartRepository.findByUserIdWithItems(userId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cartService.clearCart(userId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_NOT_FOUND);
        }
    }
}