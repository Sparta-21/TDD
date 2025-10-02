package com.sparta.tdd.domain.order.dto;

import static com.sparta.tdd.domain.order.enums.OrderStatus.PENDING;

import com.sparta.tdd.domain.order.enums.OrderStatus;
import com.sparta.tdd.domain.orderMenu.dto.OrderMenuResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponseDto(
    UUID id,
    String customerName,
    String storeName,
    Integer price,
    String address,
    List<OrderMenuResponseDto> orderMenuList,
    LocalDateTime createdAt,
    OrderStatus orderStatus
) {
}
