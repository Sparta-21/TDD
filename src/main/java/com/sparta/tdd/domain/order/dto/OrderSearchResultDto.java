package com.sparta.tdd.domain.order.dto;

import com.sparta.tdd.domain.order.entity.Order;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;

public record OrderSearchResultDto(
    Page<UUID> idPage,
    List<Order> orders
) {

}
