package com.sparta.tdd.domain.order.service;

import com.sparta.tdd.domain.auth.UserDetailsImpl;
import com.sparta.tdd.domain.menu.entity.Menu;
import com.sparta.tdd.domain.menu.repository.MenuRepository;
import com.sparta.tdd.domain.order.dto.OrderRequestDto;
import com.sparta.tdd.domain.order.dto.OrderResponseDto;
import com.sparta.tdd.domain.order.dto.OrderSearchOptionDto;
import com.sparta.tdd.domain.order.dto.OrderStatusRequestDto;
import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.order.enums.OrderStatus;
import com.sparta.tdd.domain.order.mapper.OrderMapper;
import com.sparta.tdd.domain.order.repository.OrderRepository;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.store.repository.StoreRepository;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import com.sparta.tdd.domain.user.repository.UserRepository;
import com.sparta.tdd.global.exception.BusinessException;
import com.sparta.tdd.global.exception.ErrorCode;

import java.util.*;
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

        hasPermission(userDetails, searchOption.userId());

        //region 조회
        Page<UUID> idPage = orderRepository.findPageIds(
            pageable,
            searchOption
        );

        List<Order> loaded = orderRepository.findDetailsByIdIn(idPage.getContent());
        //endregion

        List<OrderResponseDto> content = orderMapper.toResponseList(loaded, idPage);
        return new PageImpl<>(content, pageable, idPage.getTotalElements());
    }

    private void hasPermission(
            UserDetailsImpl userDetails,
            Long userId) {

        if ((UserAuthority.isCustomer(userDetails.getUserAuthority())
            || UserAuthority.isOwner(userDetails.getUserAuthority()))
            && !userDetails.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ORDER_PERMISSION_DENIED);
        }

    }

    public OrderResponseDto getOrder(
        UserDetailsImpl userDetails,
        UUID orderId) {

        Order order = orderRepository.findDetailById(orderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        hasPermission(userDetails, order.getUser().getId());

        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponseDto createOrder(
        UserDetailsImpl userDetails,
        OrderRequestDto reqDto) {

        //region 엔티티 조회
        User foundUser = findEntity(userRepository, userDetails.getUserId());
        Store foundStore = findEntity(storeRepository, reqDto.storeId());
        List<Menu> menus = menuRepository.findAllVaildMenuIds(reqDto.getMenuIds(),
            reqDto.storeId());
        //endregion

        verifyOrderMenus(menus, reqDto.getMenuIds());

        Order order = orderMapper.toOrder(reqDto, menus, foundUser, foundStore);

        Order savedOrder = orderRepository.save(order);

        return orderMapper.toResponse(savedOrder);
    }

    @Transactional
    public OrderResponseDto nextOrderStatus(UUID orderId, UserDetailsImpl userDetails) {
        Order targetOrder = findAccessibleOrder(orderId, userDetails);

        targetOrder.nextStatus();

        return orderMapper.toResponse(targetOrder);
    }

    @Transactional
    public OrderResponseDto changeOrderStatus(UUID orderId, OrderStatusRequestDto reqDto) {
        Order targetOrder = findEntity(orderRepository, orderId);

        targetOrder.changeOrderStatus(reqDto.orderStatus());

        return orderMapper.toResponse(targetOrder);
    }

    /**
     * Dto 와 repository 조회 결과를 비교해서 누락된 메뉴가 있는지 검증
     *
     * @param menus          repository 에서 조회된 menuId, Menu map
     * @param menuIdsFromDto Dto 에서 넘어온 menuId 들
     */
    private void verifyOrderMenus(
        List<Menu> menus,
        Set<UUID> menuIdsFromDto) {
        if (menus.size() != menuIdsFromDto.size()) {
            throw new BusinessException(ErrorCode.MENU_INVALID_INFO);
        }
    }

    /*
    ErrorCode.ENTITY_NOT_FOUND 를 마주쳤을때 어떤 repo 에서 발생했는지 확인 가능한가?
    OrderService 에서 User 나 Store Repo 를 직접 조회하는게 아니라 다른 Service 에서 받아오는게 책임분리 면에서 옳다
    제네릭을 사용할때는 범용성 고려필요 (범용성 <- 특정기술에 종속되서는 안 된다)
     */
    /**
     * 특정 레포지토리의 id 탐색결과를 Optional로 받아 null 이면 예외를 발생<br>
     * 값이 존재한다면 Entity 를 반환합니다
     *
     * @param jpaRepository
     * @param id
     * @return Entity
     */
    private <T, ID> T findEntity(JpaRepository<T, ID> jpaRepository, ID id) {
        return jpaRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
    }

    /**
     * OWNER 권한을 가진 유저 - Store.User.id 를 비교하여 동일하지 않으면 예외처리 (repo 에서 가져온 order 가 없음) <br>
     * MANAGER, MASTER - 별도 join 쿼리 없이 order 객체 조회
     *
     * @param orderId
     * @param userDetails
     * @return Entity
     */
    private Order findAccessibleOrder(UUID orderId, UserDetailsImpl userDetails) {
        if (UserAuthority.isOwner(userDetails.getUserAuthority())) {
            return orderRepository.findOrderByIdAndStoreUserId(orderId,
                    userDetails.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_PERMISSION_DENIED));
        }
        return orderRepository.findDetailById(orderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

    }

    private void validatePermission(UserDetailsImpl userDetails, Order order) {
        switch (userDetails.getUserAuthority()) {
            case OWNER -> hasPermission(userDetails, order.getStore().getUser().getId());
            case CUSTOMER -> hasPermission(userDetails, order.getUser().getId());
        }
    }

    @Transactional
    public OrderResponseDto cancelOrder(UUID orderId, UserDetailsImpl userDetails) {
        Order targetOrder = orderRepository.findDetailById(orderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        validatePermission(userDetails, targetOrder);

        if (!targetOrder.canCancel()) {
            throw new BusinessException(ErrorCode.ORDER_CANCELLATION_NOT_ALLOWED);
        }
        targetOrder.delete(userDetails.getUserId());
        targetOrder.changeOrderStatus(OrderStatus.CANCELLED);

        return orderMapper.toResponse(targetOrder);
    }
}
