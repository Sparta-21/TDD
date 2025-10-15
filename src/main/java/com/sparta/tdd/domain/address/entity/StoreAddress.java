package com.sparta.tdd.domain.address.entity;

import com.sparta.tdd.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "p_store_address")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreAddress extends BaseAddress{

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Builder
    public StoreAddress(String address, String roadAddress, String detailAddress, BigDecimal latitude, BigDecimal longitude) {
        super(address, roadAddress, detailAddress, latitude, longitude);
    }
}
