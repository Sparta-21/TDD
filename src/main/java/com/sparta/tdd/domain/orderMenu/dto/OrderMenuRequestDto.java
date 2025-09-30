package com.sparta.tdd.domain.orderMenu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.UUID;

public record OrderMenuRequestDto(
    @NotNull UUID menuId,
    @NotBlank String name,
    @PositiveOrZero Integer price,
    @Positive Integer quantity
) {

}
