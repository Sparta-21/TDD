package com.sparta.tdd.domain.order.dto;

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
    LocalDateTime createdAt
) {
}
