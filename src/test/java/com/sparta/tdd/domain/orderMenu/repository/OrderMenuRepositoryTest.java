package com.sparta.tdd.domain.orderMenu.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.order.repository.OrderRepository;
import com.sparta.tdd.domain.orderMenu.entity.OrderMenu;
import com.sparta.tdd.global.config.AuditConfig;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AuditConfig.class)
class OrderMenuRepositoryTest {

    @Autowired
    private OrderMenuRepository orderMenuRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private EntityManager em;

    @Nested
    @DisplayName("OrderMenu CRUD 테스트")
    class TestCRUD {

        @Test
        @DisplayName("OrderMenu 저장 및 조회")
        void saveAndFind() {
            // given
            Order order = new Order();
            order.setId(1L);
            orderRepository.save(order);

            OrderMenu orderMenu = new OrderMenu();
            orderMenu.setId(100L);
            orderMenu.setOrder(order);
            orderMenu.setQuantity(2);

            orderMenuRepository.save(orderMenu);

            // when
            Optional<OrderMenu> found = orderMenuRepository.findById(100L);

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getQuantity()).isEqualTo(2);
            assertThat(found.get().getOrder().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("OrderMenu Dirty Checking 확인")
        void update() {
            // given
            Order order = new Order();
            order.setId(2L);
            orderRepository.save(order);

            OrderMenu orderMenu = new OrderMenu();
            orderMenu.setId(200L);
            orderMenu.setOrder(order);
            orderMenu.setQuantity(1);
            orderMenuRepository.save(orderMenu);

            // when
            OrderMenu found = orderMenuRepository.findById(200L).get();
            found.setQuantity(5);

            em.flush();
            em.clear();

            OrderMenu updated = orderMenuRepository.findById(200L).get();

            // then
            assertThat(updated.getQuantity()).isEqualTo(5);
        }
    }
}
