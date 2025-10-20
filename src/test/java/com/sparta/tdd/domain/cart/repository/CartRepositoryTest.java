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

class CartRepositoryTest extends RepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private TestEntityManager em;

    private User user;
    private Store store;
    private Menu menu;

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
        menu = Menu.builder()
                .name("김치찌개")
                .description("맛있는 김치찌개")
                .price(8000)
                .imageUrl(null)
                .store(store)
                .build();
        em.persist(menu);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("사용자 ID로 삭제되지 않은 장바구니 조회 - 성공")
    void findByCart() {
        // given
        Cart cart = Cart.builder()
                .user(user)
                .build();
        em.persist(cart);
        em.flush();
        em.clear();

        // when
        Optional<Cart> result = cartRepository.findByUserIdAndNotDeleted(user.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("사용자 ID로 삭제되지 않은 장바구니 조회 - 장바구니가 없을 때")
    void emptyCart() {
        // when
        Optional<Cart> result = cartRepository.findByUserIdAndNotDeleted(user.getId());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("사용자 ID로 장바구니와 아이템 함께 조회 - 성공")
    void findByUserIdWithItems_Success() {
        // given
        Cart cart = Cart.builder()
                .user(user)
                .build();
        em.persist(cart);

        CartItem cartItem = CartItem.builder()
                .menu(menu)
                .quantity(2)
                .price(menu.getPrice())
                .build();
        cart.addCartItem(cartItem, store);

        em.persist(cartItem);
        em.flush();
        em.clear();

        // when
        Optional<Cart> result = cartRepository.findByUserIdWithItems(user.getId());

        // then
        assertThat(result).isPresent();
        Cart foundCart = result.get();
        assertThat(foundCart.getUser().getId()).isEqualTo(user.getId());
        assertThat(foundCart.getCartItems()).hasSize(1);
        assertThat(foundCart.getCartItems().get(0).getMenu().getName()).isEqualTo("김치찌개");
        assertThat(foundCart.getStore()).isNotNull();
        assertThat(foundCart.getStore().getName()).isEqualTo("테스트 가게");
    }

    @Test
    @DisplayName("장바구니 저장 - 성공")
    void save_Success() {
        // given
        Cart cart = Cart.builder()
                .user(user)
                .build();

        // when
        Cart savedCart = cartRepository.save(cart);
        em.flush();
        em.clear();

        // then
        assertThat(savedCart.getId()).isNotNull();
        Optional<Cart> found = cartRepository.findById(savedCart.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("장바구니 삭제 - Soft Delete")
    void delete_SoftDelete() {
        // given
        Cart cart = Cart.builder()
                .user(user)
                .build();
        em.persist(cart);
        em.flush();
        em.clear();

        // when
        Cart foundCart = cartRepository.findById(cart.getId()).orElseThrow();
        foundCart.delete(user.getId());
        em.flush();
        em.clear();

        // then
        Optional<Cart> notDeletedCart = cartRepository.findByUserIdAndNotDeleted(user.getId());
        assertThat(notDeletedCart).isEmpty();

        // 실제 데이터는 존재함
        Optional<Cart> actualCart = cartRepository.findById(cart.getId());
        assertThat(actualCart).isPresent();
        assertThat(actualCart.get().isDeleted()).isTrue();
    }
}