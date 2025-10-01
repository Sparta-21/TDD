package com.sparta.tdd.domain.order.dto;

import com.sparta.tdd.domain.orderMenu.dto.OrderMenuRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

public record OrderRequestDto(
    @NotBlank String address,
    @NotBlank String customerName,
    @NotBlank String storeName,
    @PositiveOrZero Integer price,
    @NotEmpty @Valid List<OrderMenuRequestDto> menu
) {

}
