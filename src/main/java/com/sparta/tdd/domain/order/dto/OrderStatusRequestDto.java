package com.sparta.tdd.domain.order.dto;

import com.sparta.tdd.domain.order.enums.OrderStatus;
import jakarta.validation.constraints.NotBlank;

public record OrderStatusRequestDto(
    @NotBlank OrderStatus orderStatus
) {

}
