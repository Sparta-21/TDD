package com.sparta.tdd.domain.cart.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record CartItemRequestDto(
        @NotNull(message = "메뉴 ID는 필수입니다.")
        UUID menuId,

        @NotNull(message = "수량은 필수입니다.")
        @Positive(message = "수량은 1개 이상이어야 합니다.")
        Integer quantity
) {}