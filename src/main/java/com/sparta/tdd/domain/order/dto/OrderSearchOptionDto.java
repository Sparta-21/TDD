package com.sparta.tdd.domain.order.dto;

import com.sparta.tdd.domain.order.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;

@Schema(description = "검색조건 DTO")
public record OrderSearchOptionDto(

    @Schema(
        description = "검색 시작 날짜 (YYYY-MM-DD 형식)",example = "2025-10-01"
    )
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate from,

    @Schema(
        description = "검색 종료 날짜 (YYYY-MM-DD 형식)",example = "2025-10-17"
    )
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate to,

    @Schema(
        description = "사용자의 고유 식별자 (Long) (고객 또는 점주)",example = "1001"
    )
    Long userId,

    @Schema(
        description = "주문 상태 (예: PENDING, CONFIRMED, COMPLETED, CANCELLED)",example = "PENDING"
    )
    OrderStatus status,

    @Schema(
        description = "가게의 고유 식별자 (UUID)",example = "8a2f47ab-31d2-4c3d-bb4e-2e2aafbbf403"
    )
    UUID storeId
) {

    public LocalDateTime startOrNull() {
        return (from != null) ? from.atStartOfDay() : null;
    }

    public LocalDateTime endOrNull() {
        return (to != null) ? to.plusDays(1).atStartOfDay() : null;
    }

}
