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

    public static boolean isCustomer(UserAuthority authority) {
        return authority == CUSTOMER;
    }

    public static boolean isOwner(UserAuthority authority) {
        return authority == OWNER;
    }

    public static boolean isManager(UserAuthority authority) {
        return authority == MANAGER;
    }

    public static boolean isMater(UserAuthority authority) {
        return authority == MASTER;
    }
}
