package com.sparta.tdd.domain.order.dto;

import com.sparta.tdd.domain.order.enums.OrderStatus;
import com.sparta.tdd.domain.orderMenu.dto.OrderMenuResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "주문 응답 DTO")
public record OrderResponseDto(

    @Schema(
        description = "주문의 고유 식별자 (UUID)", example = "550e8400-e29b-41d4-a716-446655440000"
    )
    UUID id,

    @Schema(
        description = "주문자 이름", example = "홍길동"
    )
    String customerName,

    @Schema(
        description = "가게 이름", example = "TDD 중국집"
    )
    String storeName,

    @Schema(
        description = "총 주문 금액", example = "45000"
    )
    Integer price,

    @Schema(
        description = "배달 주소", example = "서울특별시 강남구 테헤란로 427 (삼성동)"
    )
    String address,

    @Schema(
        description = "주문한 메뉴 목록",
        example = """
        [
            {
                "menuId": "7f1d9a1c-6e2b-4a12-8b9e-9c5b76a93e11",
                "name": "탕수육",
                "price": 20000,
                "quantity": 1
            },
            {
                "menuId": "b1229a0a-3a9c-4f91-8d72-1231f8bca222",
                "name": "깐풍기",
                "price": 25000,
                "quantity": 2
            }
        ]
        """
    )
    List<OrderMenuResponseDto> orderMenuList,

    @Schema(
        description = "주문 생성 시각 (ISO-8601 형식)", example = "2025-09-29T12:34:56"
    )
    LocalDateTime createdAt,

    @Schema(
        description = "주문 상태", example = "DELIVERED"
    )
    OrderStatus orderStatus
) {

}

