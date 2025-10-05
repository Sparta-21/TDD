package com.sparta.tdd.domain.order.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.sparta.tdd.domain.auth.UserDetailsImpl;
import com.sparta.tdd.domain.menu.dto.MenuRequestDto;
import com.sparta.tdd.domain.menu.entity.Menu;
import com.sparta.tdd.domain.menu.repository.MenuRepository;
import com.sparta.tdd.domain.order.dto.OrderRequestDto;
import com.sparta.tdd.domain.order.dto.OrderResponseDto;
import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.order.mapper.OrderMapper;
import com.sparta.tdd.domain.order.mapper.OrderMapperImpl;
import com.sparta.tdd.domain.order.repository.OrderRepository;
import com.sparta.tdd.domain.orderMenu.dto.OrderMenuRequestDto;
import com.sparta.tdd.domain.orderMenu.entity.OrderMenu;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.util.ReflectionTestUtils;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
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

    @BeforeEach
    void setUp() {
        // OrderMapperImpl 직접 생성 + orderMenuMapper 수동 주입
        OrderMapperImpl impl = new OrderMapperImpl();
        OrderMenuMapper orderMenuMapper = Mappers.getMapper(OrderMenuMapper.class);
        ReflectionTestUtils.setField(impl, "orderMenuMapper", orderMenuMapper);
        this.orderMapper = impl;

        orderService = new OrderService(orderRepository, userRepository, storeRepository,
            orderMapper, menuRepository);
    }

    @Test
    void createOrder() throws Exception {

        // given
        // ---- User ----
        User user = User.builder()
            .username("tester")
            .password("pw")
            .nickname("nick")
            .build();
        // 가짜 id 강제 세팅
        ReflectionTestUtils.setField(user, "id", 1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDetailsImpl userDetails = new UserDetailsImpl(
            1L,                           // userId
            "testUser",                   // username
            UserAuthority.CUSTOMER             // enum UserAuthority
        );

        System.out.println("userId: " + user.getId());

        // ---- Store ----
        Store store = Store.builder()
            .name("치킨집")
            .description("맛집")
            .build();
        ReflectionTestUtils.setField(store, "id", UUID.randomUUID());

        when(storeRepository.findByName("치킨집")).thenReturn(Optional.of(store));

        // ---- Menu ----
        // 후라이드 메뉴
        MenuRequestDto friedDto = new MenuRequestDto("후라이드", "바삭한 후라이드 치킨", 15000, null);
        Menu friedMenu = Menu.builder()
            .dto(friedDto)
            .store(store)
            .build();
        UUID friedId = UUID.randomUUID();
        ReflectionTestUtils.setField(friedMenu, "id", friedId);

        // 양념치킨 메뉴
        MenuRequestDto seasonedDto = new MenuRequestDto("양념치킨", "매콤달콤 양념치킨", 16000, null);
        Menu seasonedMenu = Menu.builder()
            .dto(seasonedDto)
            .store(store)
            .build();
        UUID seasonedId = UUID.randomUUID();
        ReflectionTestUtils.setField(seasonedMenu, "id", seasonedId);

        // repository에서 두 메뉴 반환되도록 모킹
        when(menuRepository.findAllById(any())).thenReturn(List.of(friedMenu, seasonedMenu));

        // ---- save() 모킹: Order.id + 각 OrderMenu.id 부여 ----
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            // Order PK
            ReflectionTestUtils.setField(o, "id", UUID.randomUUID());
            // Cascade 없이도 OrderMenu PK 강제 세팅
            for (OrderMenu om : o.getOrderMenuList()) {
                if (om.getId() == null) {
                    ReflectionTestUtils.setField(om, "id", UUID.randomUUID());
                }
            }
            return o;
        });

        // ---- Request DTO (새 구조 반영) ----
        OrderMenuRequestDto friedReq = new OrderMenuRequestDto(
            friedId,
            "후라이드",
            friedMenu.getPrice(),
            2 // 후라이드 2마리
        );

        OrderMenuRequestDto seasonedReq = new OrderMenuRequestDto(
            seasonedId,
            "양념치킨",
            seasonedMenu.getPrice(),
            3 // 양념치킨 3마리
        );

        OrderRequestDto reqDto = new OrderRequestDto(
            "서울시 강남구",
            "tester",
            "치킨집",
            15000 * 2 + 16000 * 3,  // 총액: 15000*2 + 16000*3 = 78000
            List.of(friedReq, seasonedReq)
        );



        // when
        OrderResponseDto response = orderService.createOrder(userDetails, reqDto);

        System.out.println("===== OrderResponseDto =====");
        System.out.println("id = " + response.id());
        System.out.println("customerName = " + response.customerName());
        System.out.println("storeName = " + response.storeName());
        System.out.println("price = " + response.price());
        System.out.println("address = " + response.address());
        System.out.println("orderMenuList = " + response.orderMenuList());
        System.out.println("createdAt = " + response.createdAt());
        System.out.println("============================");

        System.out.println(
            new com.fasterxml.jackson.databind.ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(response)
        );

        // then
        assertThat(response).isNotNull();
        assertThat(response.customerName()).isEqualTo(user.getUsername());
        assertThat(response.storeName()).isEqualTo(store.getName());
        assertThat(response.price()).isEqualTo(78000);

    }
}