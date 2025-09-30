package com.sparta.tdd.domain.order.service;

import com.sparta.tdd.domain.auth.UserDetailsImpl;
import com.sparta.tdd.domain.menu.entity.Menu;
import com.sparta.tdd.domain.menu.repository.MenuRepository;
import com.sparta.tdd.domain.order.dto.OrderRequestDto;
import com.sparta.tdd.domain.order.dto.OrderResponseDto;
import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.order.mapper.OrderMapper;
import com.sparta.tdd.domain.order.repository.OrderRepository;
import com.sparta.tdd.domain.orderMenu.dto.OrderMenuRequestDto;
import com.sparta.tdd.domain.orderMenu.entity.OrderMenu;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.store.repository.StoreRepository;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final OrderMapper orderMapper;
    private final MenuRepository menuRepository;

    public OrderResponseDto getOrder(UUID orderId) {
        return null;
    }

    @Transactional
    public OrderResponseDto createOrder(
        UserDetailsImpl userDetails,
        OrderRequestDto reqDto) {

        User foundUser = userRepository.findById(userDetails.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Store foundStore = storeRepository.findByName(reqDto.storeName())
            .orElseThrow(() -> new IllegalArgumentException("가게이름을 찾을 수 없습니다."));

        Order order = orderMapper.toOrder(reqDto);
        order.assignUser(foundUser);
        order.assignStore(foundStore);

        List<UUID> menuIds = reqDto.menu().stream()
            .map(OrderMenuRequestDto::menuId)
            .toList();

        List<Menu> menus = menuRepository.findAllById(menuIds);
        Map<UUID, Menu> menuMap = menus.stream()
                .collect(Collectors.toMap(Menu::getId, Function.identity()));

        // 빠진 메뉴(존재하지 않는 menuId) 검증
        if (menuMap.size() != menuIds.size()) {
            // 어떤 id가 빠졌는지 알려주면 디버깅에 좋음
            List<UUID> missing = new ArrayList<>(menuIds);
            missing.removeAll(menuMap.keySet());
            throw new IllegalArgumentException("존재하지 않는 메뉴가 포함되어 있습니다: " + missing);
        }

        for (OrderMenuRequestDto om : reqDto.menu()) {
            Menu menu = menuMap.get(om.menuId());

            if (!menu.getStore().getId().equals(foundStore.getId())) {
                throw new IllegalArgumentException("해당 가게의 메뉴가 아닙니다: menuId=" + om.menuId());
            }

            OrderMenu orderMenu = OrderMenu.builder()
                .quantity(om.quantity())
                .price(om.price())     // 단가 저장으로 바꾸고 싶으면 menu.getPrice()만 넣기
                .menu(menu)
                .build();

            order.addOrderMenu(orderMenu);
            orderMenu.assignMenu(menu);
        }

        Order savedOrder = orderRepository.save(order);

        OrderResponseDto resDto = orderMapper.toResponse(savedOrder);

        return resDto;
    }
}
