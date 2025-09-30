package com.sparta.tdd.domain.order.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sparta.tdd.domain.order.entity.Order;
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
}