package com.sparta.tdd.domain.order.dto;

import com.sparta.tdd.domain.orderMenu.dto.OrderMenuRequestDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Schema(description = "주문 생성 요청 DTO")
public record OrderRequestDto(

    @NotBlank
    @Schema(
        description = "배달 주소", example = "서울특별시 강남구 테헤란로 427 (삼성동)"
    )
    String address,

    @NotBlank
    @Schema(
        description = "주문자 이름", example = "홍길동"
    )
    String customerName,

    @NotNull
    @Schema(
        description = "주문할 가게의 고유 식별자 (UUID)", example = "550e8400-e29b-41d4-a716-446655440000"
    )
    UUID storeId,

    @NotBlank
    @Schema(
        description = "가게 이름", example = "TDD 중국집"
    )
    String storeName,

    @PositiveOrZero
    @Schema(
        description = "총 주문 금액", example = "24000"
    )
    Integer price,

    @NotEmpty
    @Valid
    @Schema(
        description = "주문한 메뉴 목록",
        example = """
        [
            {
                "menuId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                "name": "탕수육",
                "price": 20000,
                "quantity": 1
            },
            {
                "menuId": "4aa95f64-5717-4562-b3fc-2c963f66afb7",
                "name": "깐풍기",
                "price": 25000,
                "quantity": 2
            }
        ]
        """
    )
    List<OrderMenuRequestDto> menu
) {

    /**
     * 주문 메뉴 ID Set 반환
     *
     * @return Set<UUID> 주문 메뉴 ID Set
     */
    public Set<UUID> getMenuIds() {
        return menu.stream()
            .map(OrderMenuRequestDto::menuId)
            .collect(java.util.stream.Collectors.toSet());
    }
}
