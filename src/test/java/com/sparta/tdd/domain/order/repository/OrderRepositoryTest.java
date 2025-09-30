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
            Order order = new Order();
            order.setId(123L);
            orderRepository.save(order);

            Optional<Order> found = orderRepository.findById(123L);

            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(123L);
        }

        @Test
        @DisplayName("Dirty Checking 확인")
        void update() {
            Order order = new Order();
            order.setId(123L);
            order.setPrice(1000);
            orderRepository.save(order);

            Order found = orderRepository.findById(123L).get();

            found.setPrice(2000);

            em.flush();
            em.clear();

            Order updated = orderRepository.findById(123L).get();
            assertThat(updated.getPrice()).isEqualTo(2000);
        }

        @Test
        @DisplayName("Id 로 삭제상태 변경 확인")
        void delete() {
            Order order = new Order();
            order.setId(123L);
            orderRepository.save(order);

            Order found = orderRepository.findById(123L).get();
            found.delete(11L);

            assertThat(found.isDeleted()).isTrue();
            assertThat(orderRepository.findById(123L)).isPresent();
            assertThat(found.getDeletedBy()).isEqualTo(11L);
        }
    }
}