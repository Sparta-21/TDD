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
import org.springframework.data.jpa.repository.JpaRepository;
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
            searchOption
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

    public OrderResponseDto getOrder(
        UserDetailsImpl userDetails,
        UUID orderId) {
        Order order = orderRepository.findDetailById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("주문내역을 찾을 수 없습니다"));

        OrderResponseDto resDto = orderMapper.toResponse(order);

        return resDto;
    }

    // StoreId 도 요청 dto 에서 받아와서 isPresent 메서드로 한번에 해결
    @Transactional
    public OrderResponseDto createOrder(
        UserDetailsImpl userDetails,
        OrderRequestDto reqDto) {

        //region 엔티티 조회
        // ==== Store, User 조회 및 가져옴 ====
            User foundUser = isPresent(userRepository, userDetails.getUserId());
        Store foundStore = isPresent(storeRepository, reqDto.storeId());

        // ==== OrderMenu Entity ====
        // ==== Required Menu find ====
        // extract requested menuList from dto
        List<UUID> menuIds = reqDto.menu().stream()
            .map(OrderMenuRequestDto::menuId)
            .toList();
        // get requested menuList from repo
        List<Menu> menus = menuRepository.findAllById(menuIds);
        Map<UUID, Menu> menuMap = menus.stream()
            .collect(Collectors.toMap(Menu::getId, Function.identity()));
        //endregion

        // menu verifying (검증)
        verifyOrderMenus(menuMap, menuIds);

        /*
        OrderMenuList 인자로 받아서 내부메서드 assignMenu 돌리는걸
        OrderEntity 도메인 메서드로 만들자
        Entity 의 책임은 연관관계 설정과 컬렉션 관리라 틀린 부분은 없다고 생각됨
         */
        //region 매핑
        // ==== Order 매핑 ====
        Order order = orderMapper.toOrder(reqDto);
        order.assignUser(foundUser);
        order.assignStore(foundStore);

        // menuToOrderMenu and mapping order.orderMenuList
        // orderMenu 를 만들고 오더에 각각 매핑중
        // orderMenuList 를 한번에 만들어서 Order 에 등록하면 안 되는건가?
        // 안 된다 각 orderMenu 에 Order 외래키 설정 필요
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
        //endregion

        // ====== 저장 ======
        Order savedOrder = orderRepository.save(order);

        // ==== 응답 객체 변환 ====
        OrderResponseDto resDto = orderMapper.toResponse(savedOrder);

        return resDto;
    }

    /**
     * Dto 와 repository 조회 결과를 비교해서 누락된 메뉴가 있는지 검증
     * @param menuMap repository 에서 조회된 menuId, Menu map
     * @param menuIds Dto 에서 넘어온 menuId 들
     */
    private static void verifyOrderMenus(Map<UUID, Menu> menuMap, List<UUID> menuIds) {

        // 이전에 리뷰 받았던 대로 size 비교가 아닌 Set 을 통해 존재하는 Id 인지 확인하도록 변경하기
        if (menuMap.size() != menuIds.size()) {
            // 어떤 id가 빠졌는지 알려주면 디버깅에 좋음
            List<UUID> missing = new ArrayList<>(menuIds);
            missing.removeAll(menuMap.keySet());
            throw new IllegalArgumentException("존재하지 않는 메뉴가 포함되어 있습니다: " + missing);
        }
    }

    /**
     * 특정 레포지토리의 id 탐색결과를 Optional로 받아</br>
     * null 이면 예외를 발생</br>
     * 값이 존대한다면 Entity 를 반환합니다
     *
     * @param jpaRepository
     * @param id
     * @return Entity
     * @param <T>
     * @param <ID>
     */
    private <T, ID> T isPresent(JpaRepository<T, ID> jpaRepository, ID id) {
        return jpaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 id 입니다"));
    }
}
