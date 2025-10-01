package com.sparta.tdd.domain.orderMenu.mapper;

import com.sparta.tdd.domain.order.dto.OrderResponseDto;
import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.orderMenu.dto.OrderMenuRequestDto;
import com.sparta.tdd.domain.orderMenu.dto.OrderMenuResponseDto;
import com.sparta.tdd.domain.orderMenu.entity.OrderMenu;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMenuMapper {

    @Mapping(source = "menu.name", target = "name")
    OrderMenuResponseDto toResponse(OrderMenu orderMenu);
}
