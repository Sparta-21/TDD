package com.sparta.tdd.domain.order.service;

import com.sparta.tdd.domain.order.dto.OrderSearchOptionDto;
import com.sparta.tdd.domain.order.dto.OrderSearchResultDto;
import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.order.repository.OrderRepository;
import com.sparta.tdd.global.exception.BusinessException;
import com.sparta.tdd.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderUtilService {

    private final OrderRepository orderRepository;

    public OrderSearchResultDto searchOrdersByOption(
        Pageable pageable,
        OrderSearchOptionDto searchOption) {

        Page<UUID> idPage = orderRepository.findPageIds(
            pageable,
            searchOption
        );

        List<Order> loaded = orderRepository.findDetailsByIdIn(idPage.getContent());

        return new OrderSearchResultDto(idPage, loaded);
    }

    public Order loadOrderWithAllRelations(UUID id) {

        return orderRepository.findDetailById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    public Page<Order> searchOrdersByUserIdAndNotDeleted(Long userId, Pageable pageable) {
        return orderRepository.findOrdersByUserIdAndNotDeleted(userId, pageable);
    }

    public List<UUID> searchOrderIdsByUserIdAndNotDeleted(Long userId) {
        return orderRepository.findOrderIdsByUserIdAndDeletedAtIsNull(userId);
    }

    public void bulkSoftDeleteOrdersByUserId(
        Long userId,
        LocalDateTime deletedAt,
        Long deletedBy) {
        orderRepository.bulkSoftDeleteByUserId(userId, deletedAt, deletedBy);
    }

    public Order findOrderByIdAndStoreUserId(UUID orderId, Long userId) {

        return orderRepository.findOrderByIdAndStoreUserId(orderId, userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    public Order findById(UUID id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    public void save(Order order) {
        orderRepository.save(order);
    }

}
