package com.sparta.tdd.domain.order.repository;

import static com.sparta.tdd.domain.store.enums.StoreCategory.CHINESE;
import static com.sparta.tdd.domain.user.enums.UserAuthority.CUSTOMER;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sparta.tdd.domain.menu.dto.MenuRequestDto;
import com.sparta.tdd.domain.menu.entity.Menu;
import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.order.enums.OrderStatus;
import com.sparta.tdd.domain.orderMenu.entity.OrderMenu;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.global.config.AuditConfig;
import com.sparta.tdd.global.config.QueryDSLConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({AuditConfig.class, QueryDSLConfig.class})
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private EntityManager em;


    @Nested
    @DisplayName("주문 CRUD 테스트")
    class testCRUD {

        @Test
        @DisplayName("ID 로 주문 조회")
        void findId() {
            Order order = Order.builder()
                .build();
            Order saved = orderRepository.save(order);

            Optional<Order> found = orderRepository.findById(saved.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(saved.getId());
        }

        @Test
        @DisplayName("Dirty Checking 확인")
        void update() {
            Order order = Order.builder()
                .build();
            Order saved = orderRepository.save(order);

            Order found = orderRepository.findById(saved.getId()).get();

            em.flush();
            em.clear();

            Order updated = orderRepository.findById(saved.getId()).get();
        }

        @Test
        @DisplayName("Id 로 삭제상태 변경 확인")
        void delete() {
            Order order = Order.builder()
                .build();
            Order saved = orderRepository.save(order);

            Order found = orderRepository.findById(saved.getId()).get();
            found.delete(11L);

            assertThat(found.isDeleted()).isTrue();
            assertThat(orderRepository.findById(saved.getId())).isPresent();
            assertThat(found.getDeletedBy()).isEqualTo(11L);
        }
    }

    @Test
    void findDetailById_fetchJoin_graphLoaded() throws JsonProcessingException {
        // === given ===
        // 1) 기초 엔티티 persist
        User user = User.builder()
            .username("tester")
            .password("pw")
            .nickname("nick")
            .authority(CUSTOMER)
            .build();

        Store store = Store.builder()
            .name("치킨집")
            .description("맛집")
            .category(CHINESE)
            .user(user).build();

        em.persist(user);
        em.persist(store);

        Menu fried = Menu.builder()
            .dto(new MenuRequestDto("후라이드", "바삭", 15_000, null))
            .store(store)
            .build();

        Menu seasoned = Menu.builder()
            .dto(new MenuRequestDto("양념치킨", "매콤", 16_000, null))
            .store(store)
            .build();

        em.persist(fried);
        em.persist(seasoned);

        // 2) Order + OrderMenu 구성 (양방향 고정)
        Order order = Order.builder()
            .address("서울시 강남구")
            .orderStatus(OrderStatus.PENDING)
            .orderMenuList(new ArrayList<>())
            .store(store)
            .user(user)
            .build();

        OrderMenu om1 = OrderMenu.builder()
            .menu(fried)
            .quantity(2)
            .price(fried.getPrice())
            .build();
        OrderMenu om2 = OrderMenu.builder()
            .menu(seasoned)
            .quantity(3)
            .price(seasoned.getPrice())
            .build();

        order.addOrderMenu(om1);
        order.addOrderMenu(om2);

        em.persist(order);
        em.flush();
        em.clear(); // ★ 1차 캐시 제거: fetch join 효과를 순수 쿼리로 검증

        UUID orderId = orderRepository
            .findAll() // 단 하나니까 이렇게 꺼내도 됨 (혹은 위에서 order.getId() 보관)
            .get(0)
            .getId();

        // === when ===
        Order found = orderRepository.findDetailById(orderId).orElseThrow();

        // === then ===
        PersistenceUnitUtil util = em.getEntityManagerFactory().getPersistenceUnitUtil();

        System.out.println("=== Order 확인 ===");
        System.out.println("orderId   = " + found.getId());
        System.out.println("address   = " + found.getAddress());
        System.out.println("status    = " + found.getOrderStatus());
        System.out.println("user      = " + (found.getUser() != null ? found.getUser().getNickname() : null));
        System.out.println("store     = " + (found.getStore() != null ? found.getStore().getName() : null));
        System.out.println("payment   = " + (found.getPayment() != null ? found.getPayment().getId() : null));

        System.out.println("=== OrderMenu 목록 ===");
        for (OrderMenu om : found.getOrderMenuList()) {
            System.out.println(
                "  menu=" + om.getMenu().getName() +
                    ", price=" + om.getPrice() +
                    ", qty=" + om.getQuantity()
            );
        }


        // 연관 로딩 검증
        assertThat(util.isLoaded(found.getUser())).isTrue();
        assertThat(util.isLoaded(found.getStore())).isTrue();
        assertThat(util.isLoaded(found.getPayment())).isTrue();
        assertThat(util.isLoaded(found.getOrderMenuList())).isTrue();

        assertThat(found.getOrderMenuList()).hasSize(2);

        // 하위 연관(menu)까지 fetch join 되었는지 검증
        OrderMenu first = found.getOrderMenuList().get(0);
        assertThat(util.isLoaded(first.getMenu())).isTrue();

        // 값도 간단히 검증
        assertThat(found.getAddress()).isEqualTo("서울시 강남구");
        assertThat(found.getUser().getNickname()).isEqualTo("nick");
        assertThat(found.getStore().getName()).isEqualTo("치킨집");
        assertThat(found.getOrderMenuList())
            .extracting(om -> om.getMenu().getName())
            .containsExactlyInAnyOrder("후라이드", "양념치킨");
    }
}