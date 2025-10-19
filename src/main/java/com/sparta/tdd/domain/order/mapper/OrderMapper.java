package com.sparta.tdd.domain.order.mapper;

import com.sparta.tdd.domain.menu.entity.Menu;
import com.sparta.tdd.domain.order.dto.OrderRequestDto;
import com.sparta.tdd.domain.order.dto.OrderResponseDto;
import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.orderMenu.dto.OrderMenuRequestDto;
import com.sparta.tdd.domain.orderMenu.entity.OrderMenu;
import com.sparta.tdd.domain.orderMenu.mapper.OrderMenuMapper;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = OrderMenuMapper.class)
public interface OrderMapper {

    @Mapping(target = "orderMenuList", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "store", ignore = true)
    @Mapping(target = "orderStatus", ignore = true)
    Order toOrder(OrderRequestDto orderRequestDto);

    /*
    expression 관련해서 컴파일단계에서 파악 되지않음
     */
    @Mapping(target = "customerName", source = "user.username")
    @Mapping(target = "storeName", source = "store.name")
    @Mapping(target = "price",
        expression = "java(order.getOrderMenuList().stream()"
            + ".mapToInt(om -> om.getPrice() * om.getQuantity()).sum())")
    OrderResponseDto toResponse(Order order);


    default Order toOrder(
            OrderRequestDto reqDto,
            List<Menu> menus,
            User user,
            Store store
    ) {
        Order order = toOrder(reqDto);
        order.assignUser(user);
        order.assignStore(store);

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

    default List<OrderResponseDto> toResponseList(
            List<Order> loaded,
            Page<UUID> idPage
    ) {
        Map<UUID, Order> byId = loaded.stream()
                .collect(Collectors.toMap(Order::getId, o -> o));

        List<Order> ordered = idPage.getContent().stream()
                .map(byId::get)
                .toList();

        return ordered.stream()
                .map(this::toResponse)
                .toList();
    }
}
