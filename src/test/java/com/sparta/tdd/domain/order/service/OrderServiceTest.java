package com.sparta.tdd.domain.order.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.sparta.tdd.domain.auth.UserDetailsImpl;
import com.sparta.tdd.domain.menu.entity.Menu;
import com.sparta.tdd.domain.menu.repository.MenuRepository;
import com.sparta.tdd.domain.order.dto.OrderRequestDto;
import com.sparta.tdd.domain.order.dto.OrderResponseDto;
import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.order.mapper.OrderMapper;
import com.sparta.tdd.domain.order.mapper.OrderMapperImpl;
import com.sparta.tdd.domain.order.repository.OrderRepository;
import com.sparta.tdd.domain.orderMenu.dto.OrderMenuRequestDto;
import com.sparta.tdd.domain.orderMenu.mapper.OrderMenuMapper;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.store.repository.StoreRepository;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import com.sparta.tdd.domain.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private MenuRepository menuRepository;

    private OrderMapper orderMapper;
    private OrderService orderService;

    private User user;
    private Store store;
    private Menu friedMenu;
    private Menu seasonedMenu;
    private UUID storeUUID;
    private OrderMenuRequestDto friedReq;
    private OrderMenuRequestDto seasonedReq;
    private OrderRequestDto orderRequestDto;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        // 1. Mapper 초기화
        OrderMapperImpl impl = new OrderMapperImpl();
        ReflectionTestUtils.setField(impl, "orderMenuMapper", Mappers.getMapper(OrderMenuMapper.class));
        this.orderMapper = impl;

        // 2. Service 생성
        orderService = new OrderService(orderRepository, userRepository, storeRepository, orderMapper, menuRepository);

        // 3. 유저 세팅
        user = User.builder()
            .username("tester")
            .password("pw")
            .nickname("nick")
            .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        userDetails = new UserDetailsImpl(1L, "testUser", UserAuthority.CUSTOMER);

        // 4. 매장 세팅
        storeUUID = UUID.randomUUID();
        store = Store.builder()
            .name("치킨집")
            .description("맛집")
            .build();
        ReflectionTestUtils.setField(store, "id", storeUUID);

        // 5. 메뉴 세팅
        friedMenu = Menu.builder()
            .name("후라이드")
            .description("바삭한 후라이드 치킨")
            .price(15000)
            .imageUrl(null)
            .store(store)
            .build();
        ReflectionTestUtils.setField(friedMenu, "id", UUID.randomUUID());

        seasonedMenu = Menu.builder()
            .name("양념치킨")
            .description("매콤달콤 양념치킨")
            .price(16000)
            .imageUrl(null)
            .store(store)
            .build();
        ReflectionTestUtils.setField(seasonedMenu, "id", UUID.randomUUID());

        // 6. 주문 요청 DTO 세팅
        friedReq = new OrderMenuRequestDto(friedMenu.getId(), "후라이드", friedMenu.getPrice(), 2);
        seasonedReq = new OrderMenuRequestDto(seasonedMenu.getId(), "양념치킨", seasonedMenu.getPrice(), 3);

        orderRequestDto = new OrderRequestDto(
            "서울시 강남구",
            "tester",
            storeUUID,
            "치킨집",
            15000 * 2 + 16000 * 3,
            List.of(friedReq, seasonedReq)
        );
    }


    @Test
    @DisplayName("주문 생성: createOrder()는 주문을 정상적으로 생성해야 한다")
    void createOrder_shouldCreateNewOrderSuccessfully() {
        // given
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order order = inv.getArgument(0);
            ReflectionTestUtils.setField(order, "id", UUID.randomUUID());
            order.getOrderMenuList().forEach(om ->
                ReflectionTestUtils.setField(om, "id", UUID.randomUUID()));
            return order;
        });

        when(menuRepository.findAllVaildMenuIds(any(), eq(storeUUID)))
            .thenReturn(List.of(friedMenu, seasonedMenu));
        when(storeRepository.findById(storeUUID)).thenReturn(Optional.of(store));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        OrderResponseDto response = orderService.createOrder(userDetails, orderRequestDto);

        // then
        assertThat(response.customerName()).isEqualTo(user.getUsername());
        assertThat(response.storeName()).isEqualTo(store.getName());
        assertThat(response.price()).isEqualTo(78000);
    }
}
