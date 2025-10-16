package com.sparta.tdd.domain.order.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sparta.tdd.common.template.RepositoryTest;
import com.sparta.tdd.domain.menu.dto.MenuRequestDto;
import com.sparta.tdd.domain.menu.entity.Menu;
import com.sparta.tdd.domain.order.dto.OrderSearchOptionDto;
import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.order.enums.OrderStatus;
import com.sparta.tdd.domain.orderMenu.entity.OrderMenu;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.store.enums.StoreCategory;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;


class OrderRepositoryTest
    extends RepositoryTest
{

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private TestEntityManager em;

    User owner, other;
    Store ownerStore, otherStore;
    Order target, otherOrderSameStore, orderDifferentStore;
    Menu menu1;
    OrderMenu om1;

    private User ownerUser() {
        return User.builder()
            .username("owner")
            .password("pw")
            .nickname("owner")
            .authority(UserAuthority.OWNER)
            .build();
    }

    private User otherUser() {
        return User.builder()
            .username("other")
            .password("pw")
            .nickname("other")
            .authority(UserAuthority.OWNER)
            .build();
    }

    private Store storeOwnedBy(User user, String name, StoreCategory storeCategory) {
        return Store.builder()
            .name(name)
            .category(storeCategory)
            .description(null)
            .imageUrl(null)
            .user(user)
            .build();
    }

    private Order orderOf(Store store, User user, String addr, OrderStatus orderStatus) {
        return Order.builder()
            .address(addr)
            .orderStatus(orderStatus)
            .store(store)
            .user(user)
            .build();
    }

    private MenuRequestDto menuDto(String name, Integer price) {
        return MenuRequestDto.builder()
            .name(name)
            .description(null)
            .price(price)
            .imageUrl(null)
            .build();
    }

    private Menu menuOf(Store store, String name, Integer price) {
        return menuDto(name, price).toEntity(store);
    }

    private OrderMenu orderMenuOf(Order order, Menu menu, int qty, int price) {
        return OrderMenu.builder()
            .quantity(qty)
            .price(price)
            .order(order)
            .menu(menu)
            .build();
    }

    @BeforeEach
    void setUp() {
        owner = em.persist(ownerUser());
        other = em.persist(otherUser());
        ownerStore = em.persist(storeOwnedBy(owner,"ownerStore", StoreCategory.BAKERY));
        otherStore = em.persist(storeOwnedBy(other,"otherStore", StoreCategory.CHINESE));
        target = em.persist(orderOf(ownerStore, owner, "targetAdd", OrderStatus.PENDING));
        otherOrderSameStore = em.persist(orderOf(ownerStore, owner, "targetAdd", OrderStatus.PENDING));
        orderDifferentStore = em.persist(orderOf(otherStore, other, "targetAdd", OrderStatus.PENDING));
        menu1 = em.persist(menuOf(ownerStore, "menu-1", 5000));
        om1 = em.persist(orderMenuOf(target, menu1, 2, 10000));
        em.flush();
        em.clear();
    }

    @Nested
    @DisplayName("findOrderByIdAndStoreUserId 테스트")
    class FindOrderByIdAndStoreUserId {
        @Test
        @DisplayName("주문과 점주가 일치하면 주문을 반환한다")
        void whenOwnerAndOrderMatch_returnsOrder() {
            // When
            Optional<Order> result = orderRepository.findOrderByIdAndStoreUserId(target.getId(), owner.getId());

            // Then
            Assertions.assertAll(
                () -> assertTrue(result.isPresent()),
                () -> Assertions.assertEquals(target.getId(), result.get().getId()),
                () -> Assertions.assertEquals(owner.getId(), result.get().getStore().getUser().getId())
            );
        }
        @Test
        @DisplayName("점주 불일치면 빈값")
        void whenStoreUserIdDiff_returnsEmpty() {
            //When
            Optional<Order> result = orderRepository.findOrderByIdAndStoreUserId(target.getId(), other.getId());

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("주문 ID 가 없으면 빈값")
        void whenOrderIdDiff_returnsOrder() {
            //When
            Optional<Order> result = orderRepository.findOrderByIdAndStoreUserId(UUID.randomUUID(), owner.getId());


            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("findDetailById 테스트")
    class findDetailById {


        @Test
            @DisplayName("페치 조인으로 연관이 초기화")
            void OrderRelationInitialize() {
            //when
            Optional<Order> result = orderRepository.findDetailById(target.getId());

            System.out.println(result.get().getCreatedAt());

            assertTrue(result.isPresent());
            assertTrue(Hibernate.isInitialized(result.get().getStore()));
            assertTrue(Hibernate.isInitialized(result.get().getUser()));
            assertTrue(Hibernate.isInitialized(result.get().getOrderMenuList()));
            result.get().getOrderMenuList().forEach(
                om ->
                    assertTrue(Hibernate.isInitialized(om.getMenu()))
            );
        }
    }

    @Nested
    @DisplayName("findPageIds 테스트")
    class findPageIds {

        private void seedOrders(int ownerCount, int otherCount) {
            for (int i = 0; i < ownerCount; i++) {
                Order order = orderOf(ownerStore, owner, "addr-o-"+i, OrderStatus.PENDING);
                try { Thread.sleep(1); } catch (InterruptedException ignored) {}
                em.persist(order);
            }
            for (int i = 0; i < otherCount; i++) {
                Order order = orderOf(otherStore, other, "addr-x-"+i, OrderStatus.PENDING);
                try { Thread.sleep(1); } catch (InterruptedException ignored) {}
                em.persist(order);
            }
            em.flush(); em.clear();
        }

        @Test
        @DisplayName("전체조회: page=0,size=10, createdAt DESC → 총43, 첫페이지10, 내림차순")
        void page0_size10_sortedByCreatedAtDesc_all() {
            // given
            seedOrders(23, 17); // 총 40건 beforeEach 3건 포함 43건
            var pageable = PageRequest.of(
                0, 10, Sort.by("createdAt").descending());
            var opt = new OrderSearchOptionDto(null, null, null, null, null);

            // when
            var page = orderRepository.findPageIds(pageable, opt);

            // then
            var ids = page.getContent();
            Assertions.assertAll(
                () -> Assertions.assertEquals(10, ids.size()),
                () -> Assertions.assertEquals(43, page.getTotalElements()),
                () -> {
                    var firstTwo = ids.stream()
                            .limit(2)
                            .toList();
                    var orders = orderRepository.findDetailsByIdIn(firstTwo);
                    var map = orders.stream()
                            .collect(Collectors.toMap(Order::getId, o -> o));

                    var o0 = map.get(firstTwo.get(0));
                    var o1 = map.get(firstTwo.get(1));
                    Assertions.assertTrue(
                        !o0.getCreatedAt().isBefore(o1.getCreatedAt()),
                        "createdAt must be DESC");
                }
            );
        }

        @Test
        @DisplayName("owner 필터: page=0,size=10, createdAt DESC → 총23, 모두 owner 소유")
        void page0_size10_sortedByCreatedAtDesc_ownerOnly() {
            // given
            seedOrders(23, 17);
            var pageable = PageRequest.of(
                0, 10, Sort.by("createdAt").descending());
            var opt = new OrderSearchOptionDto(null, null, owner.getId(), null, null);

            // when
            var page = orderRepository.findPageIds(pageable, opt);

            // then
            var ids = page.getContent();
            var orders = orderRepository.findDetailsByIdIn(ids);
            var map = orders.stream().collect(Collectors.toMap(Order::getId, o -> o));
            var firstTwo = ids.stream()
                    .limit(2)
                    .toList();
            var o0 = map.get(firstTwo.get(0));
            var o1 = map.get(firstTwo.get(1));

            org.junit.jupiter.api.Assertions.assertAll(
                () -> Assertions.assertEquals(10, ids.size()),
                () -> Assertions.assertEquals(25, page.getTotalElements()),
                () -> Assertions.assertTrue(
                    orders.stream().allMatch(o -> o.getStore().getUser().getId().equals(owner.getId())),
                    "모든 결과가 owner 소유여야 함"),
                () -> Assertions.assertTrue(
                    !o0.getCreatedAt().isBefore(o1.getCreatedAt()),
                    "createdAt 내림차순 정렬")
            );
        }

    }


}