package com.sparta.tdd.domain.order.dto;

import com.sparta.tdd.domain.order.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;

public record OrderSearchOptionDto(
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate from,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate to,

    OrderStatus status,

    @NotNull
    Long userId,

    UUID storeId
) {

    public LocalDateTime startOrNull() {
        return (from != null) ? from.atStartOfDay() : null;
    }

    public LocalDateTime endOrNull() {
        return (to != null) ? to.plusDays(1).atStartOfDay() : null;
    }

}
