package com.sparta.tdd.domain.orderMenu.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sparta.tdd.common.template.RepositoryTest;
import com.sparta.tdd.domain.menu.dto.MenuRequestDto;
import com.sparta.tdd.domain.menu.entity.Menu;
import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.orderMenu.entity.OrderMenu;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.store.enums.StoreCategory;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class OrderMenuRepositoryTest extends RepositoryTest {

    @Autowired
    private OrderMenuRepository orderMenuRepository;
    @Autowired
    private EntityManager em;

    private User testUser;
    private Store testStore;
    private Order testOrder;
    private MenuRequestDto menuDto;
    private Menu testMenu;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .username("testuser")
            .password("password123")
            .nickname("테스트유저")
            .authority(UserAuthority.CUSTOMER)
            .build();
        em.persist(testUser);

        testStore = Store.builder()
            .name("테스트 가게")
            .category(StoreCategory.KOREAN)
            .description("맛있는 가게")
            .user(testUser)
            .build();
        em.persist(testStore);

        testOrder = Order.builder()
            .address("서울시 강남구 테스트동 123")
            .user(testUser)
            .store(testStore)
            .build();
        em.persist(testOrder);

        testMenu = Menu.builder()
            .name("테스트 메뉴")
            .description("맛있는 메뉴")
            .price(5000)
            .imageUrl("string")
            .store(testStore)
            .build();
        em.persist(testMenu);

        em.flush();
        em.clear();
    }

    @Nested
    @DisplayName("OrderMenu CRUD 테스트")
    class TestCRUD {

        @Test
        @DisplayName("OrderMenu 저장 및 조회")
        void saveAndFind() {
            // given
            Order order = em.find(Order.class, testOrder.getId());
            Menu menu = em.find(Menu.class, testMenu.getId());

            OrderMenu orderMenu = OrderMenu.builder()
                .quantity(2)
                .price(5000)
                .order(order)
                .menu(menu)
                .build();

            OrderMenu saved = orderMenuRepository.save(orderMenu);

            // when
            Optional<OrderMenu> found = orderMenuRepository.findById(saved.getId());

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getQuantity()).isEqualTo(2);
            assertThat(found.get().getOrder().getId()).isEqualTo(testOrder.getId());
        }

        @Test
        @DisplayName("OrderMenu Dirty Checking 확인")
        void update() {
            // given
            Order order = em.find(Order.class, testOrder.getId());
            Menu menu = em.find(Menu.class, testMenu.getId());

            OrderMenu orderMenu = OrderMenu.builder()
                .quantity(1)
                .price(5000)
                .order(order)
                .menu(menu)
                .build();
            OrderMenu saved = orderMenuRepository.save(orderMenu);

            // when
            OrderMenu found = orderMenuRepository.findById(saved.getId()).get();
            found.updateQuantity(5);

            em.flush();
            em.clear();

            OrderMenu updated = orderMenuRepository.findById(saved.getId()).get();

            // then
            assertThat(updated.getQuantity()).isEqualTo(5);
        }
    }
}
