package com.sparta.tdd.domain.point.dto;

import com.sparta.tdd.domain.point.enums.PointType;
import com.sparta.tdd.domain.user.entity.User;
import java.util.UUID;
import lombok.Builder;

@Builder
public record PointRequest(
    User user,
    UUID referenceId,
    PointType type,
    Long amount,
    String description
) {

    public static PointRequest forPayment(User user, UUID orderId, Long amount,
        String description) {
        return PointRequest.builder()
            .user(user)
            .referenceId(orderId)
            .amount(amount)
            .type(PointType.PAYMENT_EARNED)
            .description(description)
            .build();
    }

    public static PointRequest forReview(User user, UUID reviewId, Long amount,
        String description) {
        return PointRequest.builder()
            .user(user)
            .referenceId(reviewId)
            .amount(amount)
            .type(PointType.REVIEW_EARNED)
            .description(description)
            .build();
    }
}