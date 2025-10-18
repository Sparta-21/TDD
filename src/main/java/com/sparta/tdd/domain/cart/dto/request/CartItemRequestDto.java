package com.sparta.tdd.domain.cart.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

@Schema(description = "장바구니 아이템 추가 요청 DTO")
public record CartItemRequestDto(
        @NotNull(message = "메뉴 ID는 필수입니다.")
        @Schema(description = "메뉴 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID menuId,

        @NotNull(message = "수량은 필수입니다.")
        @Positive(message = "수량은 1개 이상이어야 합니다.")
        @Schema(description = "주문 수량", example = "2")
        Integer quantity
) {}