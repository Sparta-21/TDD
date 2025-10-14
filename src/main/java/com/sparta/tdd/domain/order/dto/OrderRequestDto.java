package com.sparta.tdd.domain.order.dto;

import com.sparta.tdd.domain.orderMenu.dto.OrderMenuRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record OrderRequestDto(
    @NotBlank String address,
    @NotBlank String customerName,
    @NotNull UUID  storeId,
    @NotBlank String storeName,
    @PositiveOrZero Integer price,
    @NotEmpty @Valid List<OrderMenuRequestDto> menu
) {
    /**
     * 주문 메뉴 ID Set 반환
     * @return Set<UUID> 주문 메뉴 ID Set
     */
    public Set<UUID> getMenuIds() {
        return menu.stream()
            .map(OrderMenuRequestDto::menuId)
            .collect(java.util.stream.Collectors.toSet());
    }
}
