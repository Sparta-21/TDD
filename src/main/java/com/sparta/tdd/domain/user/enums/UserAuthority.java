package com.sparta.tdd.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserAuthority {
    CUSTOMER("고객"),
    OWNER("가게 사장님"),
    MANAGER("관리자"),
    MASTER("최종 관리자");

    private final String description;

    public boolean isCustomerOrManager() {
        return this == CUSTOMER || this == MANAGER;
    }
}
