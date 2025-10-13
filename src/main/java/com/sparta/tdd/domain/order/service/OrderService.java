package com.sparta.tdd.domain.order.service;

import com.sparta.tdd.domain.auth.UserDetailsImpl;
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
import com.sparta.tdd.global.exception.BusinessException;
import com.sparta.tdd.global.exception.ErrorCode;
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
        UserDetailsImpl userDetails,
        Pageable pageable,
        OrderSearchOptionDto searchOption) {

        // 검색조건에 맞는 Id 들을 페이징처리해서 가져옴
        Page<UUID> idPage = orderRepository.findPageIds(
            pageable,
            searchOption.userId(),
            searchOption.startOrNull(),
            searchOption.endOrNull(),
            searchOption.storeId()
        );

        List<UUID> ids = idPage.getContent();

        // In 은 데이터 순서를 보장하지 않음
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

    public OrderResponseDto getOrder(UserDetailsImpl userDetails, UUID orderId) {
        Order order = orderRepository.findDetailById(orderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        OrderResponseDto resDto = orderMapper.toResponse(order);

        return resDto;
    }

    @Transactional
    public OrderResponseDto createOrder(
        UserDetailsImpl userDetails,
        OrderRequestDto reqDto) {

        User foundUser = userRepository.findById(userDetails.getUserId())
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Store foundStore = storeRepository.findByName(reqDto.storeName())
            .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

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
        verifyOrderMenus(menuMap, menuIds);

        for (OrderMenuRequestDto om : reqDto.menu()) {
            Menu menu = menuMap.get(om.menuId());

            if (!menu.getStore().getId().equals(foundStore.getId())) {
                throw new BusinessException(ErrorCode.MENU_NOT_IN_STORE, "해당 가게의 메뉴가 아닙니다: menuId=" + om.menuId());
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

    /**
     * Dto 와 repository 조회 결과를 비교해서 누락된 메뉴가 있는지 검증
     * @param menuMap repository 에서 조회된 menuId, Menu map
     * @param menuIds Dto 에서 넘어온 menuId 들
     */
    private static void verifyOrderMenus(Map<UUID, Menu> menuMap, List<UUID> menuIds) {
        if (menuMap.size() != menuIds.size()) {
            // 어떤 id가 빠졌는지 알려주면 디버깅에 좋음
            List<UUID> missing = new ArrayList<>(menuIds);
            missing.removeAll(menuMap.keySet());
            throw new BusinessException(ErrorCode.MENU_NOT_FOUND, "존재하지 않는 메뉴가 포함되어 있습니다: " + missing);
        }
    }


}
