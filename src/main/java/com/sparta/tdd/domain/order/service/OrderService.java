package com.sparta.tdd.domain.order.service;

import com.sparta.tdd.domain.menu.entity.Menu;
import com.sparta.tdd.domain.menu.repository.MenuRepository;
import com.sparta.tdd.domain.order.dto.OrderRequestDto;
import com.sparta.tdd.domain.order.dto.OrderResponseDto;
import com.sparta.tdd.domain.order.dto.OrderSearchOptionDto;
import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.order.mapper.OrderMapper;
import com.sparta.tdd.domain.order.repository.OrderRepository;
import com.sparta.tdd.domain.orderMenu.dto.OrderMenuRequestDto;
import com.sparta.tdd.domain.orderMenu.entity.OrderMenu;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.store.repository.StoreRepository;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

    public Page<OrderResponseDto> getOrders(
        Long userId,
        Pageable pageable,
        OrderSearchOptionDto searchOption) {

        LocalDateTime start = searchOption.startOrNull();
        LocalDateTime end = searchOption.endOrNull();
        UUID targetStoreId = searchOption.storeId();
        Long targetUserId = searchOption.userId();

        // 검색조건에 맞는 Id 들을 페이징처리해서 가져와야함
        Page<UUID> idPage = orderRepository.findPageIds(
            pageable,
            targetUserId,
            start,
            end,
            targetStoreId
        );

        List<UUID> ids = idPage.getContent();

        List<Order> loaded = orderRepository.findDetailsByIdIn(ids);

        // id 순서대로 재정렬
        Map<UUID, Order> byId = loaded.stream()
            .collect(Collectors.toMap(Order::getId, o -> o));
        List<Order> ordered = ids.stream()
            .map(byId::get)
            .toList();


        List<OrderResponseDto> content = ordered.stream()
            .map(orderMapper::toResponse)
            .toList();

        return new PageImpl<>(content, pageable, idPage.getTotalElements());
    }

    public OrderResponseDto getOrder(Long userId, UUID orderId) {
        Order order = orderRepository.findDetailById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("주문내역을 찾을 수 없습니다"));

        OrderResponseDto resDto = orderMapper.toResponse(order);

        return resDto;
    }

    @Transactional
    public OrderResponseDto createOrder(
        Long userId,
        OrderRequestDto reqDto) {

        User foundUser = userRepository.findById(userId)
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
                .price(om.price())
                .menu(menu)
                .build();

            order.addOrderMenu(orderMenu);
        }

        Order savedOrder = orderRepository.save(order);

        OrderResponseDto resDto = orderMapper.toResponse(savedOrder);

        return resDto;
    }
}
