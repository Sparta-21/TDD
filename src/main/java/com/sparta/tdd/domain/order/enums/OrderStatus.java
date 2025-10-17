package com.sparta.tdd.domain.order.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    CANCELLED("취소됨"){
        @Override
        public OrderStatus next() {
            throw new IllegalStateException("취소된 주문입니다");
        }
    },
    PENDING("대기") {
        @Override
        public OrderStatus next() {
            return DELIVERED;
        }
    },
    DELIVERED("배달완료") {
        @Override
        public OrderStatus next() {
            throw new IllegalStateException("이미 배달완료된 주문은 더 이상 변경 불가합니다");
        }
    };

    private final String description;

    public abstract OrderStatus next();
}
