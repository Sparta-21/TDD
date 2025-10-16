package com.sparta.tdd.domain.order.dto;

import com.sparta.tdd.domain.orderMenu.entity.OrderMenu;
import java.util.List;
import java.util.UUID;

public record OrderItemInfoDto(
    UUID id,
    String menuName,
    Integer quantity,
    Integer price,
    Integer totalPrice
) {

    public static OrderItemInfoDto from(OrderMenu orderMenu) {
        return new OrderItemInfoDto(
            orderMenu.getId(),
            orderMenu.getMenu().getName(),
            orderMenu.getQuantity(),
            orderMenu.getPrice(),
            orderMenu.getPrice() * orderMenu.getQuantity()
        );
    }

    public static List<OrderItemInfoDto> fromList(List<OrderMenu> orderMenuList) {
        return orderMenuList.stream()
            .map(OrderItemInfoDto::from)
            .toList();
    }
}
