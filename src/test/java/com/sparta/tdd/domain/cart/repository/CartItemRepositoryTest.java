package com.sparta.tdd.domain.cart.repository;

import com.sparta.tdd.common.template.RepositoryTest;
import com.sparta.tdd.domain.cart.entity.Cart;
import com.sparta.tdd.domain.cart.entity.CartItem;
import com.sparta.tdd.domain.menu.entity.Menu;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.store.enums.StoreCategory;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import com.sparta.tdd.global.config.AuditConfig;
import com.sparta.tdd.global.config.QueryDSLConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CartItemRepositoryTest extends RepositoryTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private TestEntityManager em;

    private User user;
    private Cart cart;
    private Store store;
    private Menu menu1;
    private Menu menu2;

    @BeforeEach
    void setUp() {
        // 테스트 유저 생성
        user = User.builder()
                .username("testuser")
                .password("Password1!")
                .nickname("테스트유저")
                .authority(UserAuthority.CUSTOMER)
                .build();
        em.persist(user);

        // 가게 소유자 생성
        User owner = User.builder()
                .username("owner")
                .password("Password1!")
                .nickname("사장님")
                .authority(UserAuthority.OWNER)
                .build();
        em.persist(owner);

        // 테스트 가게 생성
        store = Store.builder()
                .name("테스트 가게")
                .category(StoreCategory.KOREAN)
                .description("맛있는 한식집")
                .user(owner)
                .build();
        em.persist(store);

        // 테스트 메뉴 생성
        menu1 = Menu.builder()
                .name("김치찌개")
                .description("맛있는 김치찌개")
                .price(8000)
                .imageUrl(null)
                .store(store)
                .build();
        em.persist(menu1);

        menu2 = Menu.builder()
                .name("된장찌개")
                .description("구수한 된장찌개")
                .price(7000)
                .imageUrl(null)
                .store(store)
                .build();
        em.persist(menu2);

        // 테스트 장바구니 생성
        cart = Cart.builder()
                .user(user)
                .build();
        em.persist(cart);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("장바구니 ID와 메뉴 ID로 삭제되지 않은 아이템 조회 - 성공")
    void findByCartIdAndMenuId_Success() {
        // given
        CartItem cartItem = CartItem.builder()
                .menu(menu1)
                .quantity(2)
                .price(menu1.getPrice())
                .build();
        cart.addCartItem(cartItem, store);
        em.persist(cartItem);
        em.flush();
        em.clear();

        // when
        Optional<CartItem> result = cartItemRepository.findByCartIdAndMenuId(
                cart.getId(),
                menu1.getId()
        );

        // then
        assertThat(result).isPresent();
        CartItem foundItem = result.get();
        assertThat(foundItem.getMenu().getId()).isEqualTo(menu1.getId());
        assertThat(foundItem.getCart().getId()).isEqualTo(cart.getId());
        assertThat(foundItem.getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("장바구니 ID와 메뉴 ID로 삭제되지 않은 아이템 조회 - 아이템이 없을 때")
    void findByCartIdAndMenuId_NotFound() {
        // when
        Optional<CartItem> result = cartItemRepository.findByCartIdAndMenuId(
                cart.getId(),
                menu1.getId()
        );

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("장바구니 ID와 메뉴 ID로 삭제되지 않은 아이템 조회 - 삭제된 아이템은 조회되지 않음")
    void findByCartIdAndMenuId_DeletedItem() {
        // given
        CartItem cartItem = CartItem.builder()
                .menu(menu1)
                .quantity(2)
                .price(menu1.getPrice())
                .build();
        cart.addCartItem(cartItem, store);
        em.persist(cartItem);
        cartItem.delete(user.getId());
        em.flush();
        em.clear();

        // when
        Optional<CartItem> result = cartItemRepository.findByCartIdAndMenuId(
                cart.getId(),
                menu1.getId()
        );

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("장바구니 아이템 저장 - 성공")
    void save_Success() {
        // given
        CartItem cartItem = CartItem.builder()
                .menu(menu1)
                .quantity(3)
                .price(menu1.getPrice())
                .build();
        cart.addCartItem(cartItem, store);

        // when
        CartItem savedItem = cartItemRepository.save(cartItem);
        em.flush();
        em.clear();

        // then
        assertThat(savedItem.getId()).isNotNull();
        Optional<CartItem> found = cartItemRepository.findById(savedItem.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getQuantity()).isEqualTo(3);
        assertThat(found.get().getPrice()).isEqualTo(menu1.getPrice());
    }

    @Test
    @DisplayName("장바구니 아이템 수량 수정 - 성공")
    void updateQuantity_Success() {
        // given
        CartItem cartItem = CartItem.builder()
                .menu(menu1)
                .quantity(2)
                .price(menu1.getPrice())
                .build();
        cart.addCartItem(cartItem, store);
        em.persist(cartItem);
        em.flush();
        em.clear();

        // when
        CartItem foundItem = cartItemRepository.findById(cartItem.getId()).orElseThrow();
        foundItem.updateQuantity(5);
        em.flush();
        em.clear();

        // then
        CartItem updatedItem = cartItemRepository.findById(cartItem.getId()).orElseThrow();
        assertThat(updatedItem.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("장바구니 아이템 삭제 - Soft Delete")
    void delete_SoftDelete() {
        // given
        CartItem cartItem = CartItem.builder()
                .menu(menu1)
                .quantity(2)
                .price(menu1.getPrice())
                .build();
        cart.addCartItem(cartItem, store);
        em.persist(cartItem);
        em.flush();
        em.clear();

        // when
        CartItem foundItem = cartItemRepository.findById(cartItem.getId()).orElseThrow();
        foundItem.delete(user.getId());
        em.flush();
        em.clear();

        // then
        Optional<CartItem> notDeletedItem = cartItemRepository.findByCartIdAndMenuId(
                cart.getId(),
                menu1.getId()
        );
        assertThat(notDeletedItem).isEmpty();

        // 실제 데이터는 존재함
        Optional<CartItem> actualItem = cartItemRepository.findById(cartItem.getId());
        assertThat(actualItem).isPresent();
        assertThat(actualItem.get().isDeleted()).isTrue();
    }

}