package com.sparta.tdd.domain.order.dto;

import com.sparta.tdd.domain.order.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "주문 상태변경 요청 DTO")
public record OrderStatusRequestDto(
    @Schema(description = "변경하려고 하는 주문 상태", example = "DELIVERED")
    @NotNull OrderStatus orderStatus
) {

}
