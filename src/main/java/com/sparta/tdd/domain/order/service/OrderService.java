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
import com.sparta.tdd.domain.user.enums.UserAuthority;
import com.sparta.tdd.domain.user.repository.UserRepository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
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

        hasPermission(userDetails, searchOption);

        //region 조회
        Page<UUID> idPage = orderRepository.findPageIds(
            pageable,
            searchOption
        );

        List<Order> loaded = orderRepository.findDetailsByIdIn(idPage.getContent());
        //endregion

        List<OrderResponseDto> content = mappingOrderResponseDto(loaded, idPage);

        return new PageImpl<>(content, pageable, idPage.getTotalElements());
    }

    private void hasPermission(
            UserDetailsImpl userDetails,
            OrderSearchOptionDto searchOption) {

        if (userDetails.getUserAuthority() == UserAuthority.CUSTOMER
        && !userDetails.getUserId().equals(searchOption.userId())) {
            throw new IllegalArgumentException("권한이 없습니다");
        }

    }

    private void hasPermission(
            UserDetailsImpl userDetails,
            Long userId) {

        if (userDetails.getUserAuthority() == UserAuthority.CUSTOMER
                && !userDetails.getUserId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다");
        }

    }

    /**
     * In 으로 받아온 순서보장이 되지않은 Order Entity List 와</br>
     * Page<UUID> 의 id 순서를 맞춰서 OrderResponseDto List 로 변환합니다
     * @param loaded List<Order> - In 조회로 받아온 Order List
     * @param idPage  Page<UUID> - id 순서가 보장된 Page
     * @return OrderResponseDto List
     */
    private List<OrderResponseDto> mappingOrderResponseDto(
            List<Order> loaded,
            Page<UUID> idPage) {
        // id -> Order map 생성
        Map<UUID, Order> byId = loaded.stream()
            .collect(Collectors.toMap(Order::getId, o -> o));

        // id 순서대로 재정렬
        List<Order> ordered = idPage.getContent().stream()
            .map(byId::get)
            .toList();

        List<OrderResponseDto> content = ordered.stream()
            .map(orderMapper::toResponse)
            .toList();

        return content;
    }



    public OrderResponseDto getOrder(
        UserDetailsImpl userDetails,
        UUID orderId) {

        Order order = orderRepository.findDetailById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("주문내역을 찾을 수 없습니다"));

        hasPermission(userDetails, order.getUser().getId());

        OrderResponseDto resDto = orderMapper.toResponse(order);

        return resDto;
    }



    @Transactional
    public OrderResponseDto createOrder(
        UserDetailsImpl userDetails,
        OrderRequestDto reqDto) {

        //region 엔티티 조회
        User foundUser = isPresent(userRepository, userDetails.getUserId());
        Store foundStore = isPresent(storeRepository, reqDto.storeId());
        List<Menu> menus = menuRepository.findAllVaildMenuIds(reqDto.getMenuIds(), reqDto.storeId());
        //endregion

        verifyOrderMenus(menus, reqDto.getMenuIds());

        Order order = mappingAndRelation(reqDto, menus, foundUser, foundStore);

        Order savedOrder = orderRepository.save(order);

        OrderResponseDto resDto = orderMapper.toResponse(savedOrder);

        return resDto;
    }

    /**
     * OrderRequestDto -> Order Entity 변환 및 연관관계 설정
     * @param reqDto OrderRequestDto - 주문요청 Dto
     * @param menus List<Menu> - repository 에서 조회된 Menu Entity List
     * @param foundUser User - repository 에서 조회된 User Entity
     * @param foundStore Store - repository 에서 조회된 Store Entity
     * @return Order
     */
    private Order mappingAndRelation(
            OrderRequestDto reqDto,
            List<Menu> menus,
            User foundUser,
            Store foundStore) {
        Order order = orderMapper.toOrder(reqDto);
        order.assignUser(foundUser);
        order.assignStore(foundStore);

        Map<UUID, Menu> menuMap = menus.stream()
                .collect(Collectors.toMap(Menu::getId, Function.identity()));

        for (OrderMenuRequestDto om : reqDto.menu()) {
            OrderMenu orderMenu = OrderMenu.builder()
                .quantity(om.quantity())
                .price(om.price())
                .menu(menuMap.get(om.menuId()))
                .build();

            order.addOrderMenu(orderMenu);
        }

        return order;
    }


    /**
     * Dto 와 repository 조회 결과를 비교해서 누락된 메뉴가 있는지 검증
     * @param menus repository 에서 조회된 menuId, Menu map
     * @param menuIdsFromDto Dto 에서 넘어온 menuId 들
     */
    private void verifyOrderMenus(
            List<Menu> menus,
            Set<UUID> menuIdsFromDto) {
        if (menus.size() != menuIdsFromDto.size()) {
            throw new IllegalArgumentException("메뉴 정보가 올바르지 않습니다");
        }
    }

    /**
     * 특정 레포지토리의 id 탐색결과를 Optional로 받아</br>
     * null 이면 예외를 발생</br>
     * 값이 존재한다면 Entity 를 반환합니다
     *
     * @param jpaRepository
     * @param id
     * @return Entity
     */
    private <T, ID> T isPresent(JpaRepository<T, ID> jpaRepository, ID id) {
        return jpaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 id 입니다"));
    }
}
