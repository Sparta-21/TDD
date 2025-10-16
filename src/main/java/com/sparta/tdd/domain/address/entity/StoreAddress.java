package com.sparta.tdd.domain.address.entity;

import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.global.exception.BusinessException;
import com.sparta.tdd.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_store_address")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreAddress extends BaseAddress{

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    public StoreAddress(String address, String roadAddress, String detailAddress, Double latitude, Double longitude, Store store) {
        super(address, roadAddress, detailAddress, latitude, longitude);
        this.store = store;
    }
    public void validateOwner(User user) {
        if (!store.isOwner(user)) {
            throw new BusinessException(ErrorCode.ADDRESS_STORE_PERMISSION_DENIED);
        }
    }
}
