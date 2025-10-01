package com.sparta.tdd.domain.order.mapper;

import com.sparta.tdd.domain.order.dto.OrderRequestDto;
import com.sparta.tdd.domain.order.dto.OrderResponseDto;
import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.orderMenu.mapper.OrderMenuMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = OrderMenuMapper.class)
public interface OrderMapper {

    @Mapping(target = "orderMenuList", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "store", ignore = true)
    Order toOrder(OrderRequestDto orderRequestDto);

    @Mapping(target = "customerName", source = "user.username")
    @Mapping(target = "storeName", source = "store.name")
    @Mapping(target = "price",
        expression = "java(order.getOrderMenuList().stream().mapToInt(om -> om.getPrice()).sum())")
    OrderResponseDto toResponse(Order order);
}
