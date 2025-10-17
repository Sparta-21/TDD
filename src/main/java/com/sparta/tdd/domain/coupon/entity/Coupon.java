package com.sparta.tdd.domain.coupon.entity;

import com.sparta.tdd.domain.coupon.dto.CouponRequestDto;
import com.sparta.tdd.domain.coupon.enums.Scope;
import com.sparta.tdd.domain.coupon.enums.Type;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.global.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_coupon")
@Getter
@NoArgsConstructor
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "coupon_id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private Type type;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false, updatable = false)
    private Scope scope;

    @Column(name = "discount_value", nullable = false)
    private Integer discountValue;

    @Column(name = "min_order_price", nullable = false)
    private Integer minOrderPrice;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "issued_count", nullable = false)
    private int issuedCount = 0;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @Builder
    public Coupon(CouponRequestDto dto, Store store) {
        this.name = dto.name();
        this.type = dto.type();
        this.scope = dto.scope();
        this.discountValue = dto.discountValue();
        this.minOrderPrice = dto.minOrderPrice();
        this.quantity = dto.quantity();
        this.expiredAt = dto.expiredAt();
        this.store = store;
    }

    public void update(CouponRequestDto dto) {
        this.name = dto.name();
        this.type = dto.type();
        this.discountValue = dto.discountValue();
        this.minOrderPrice = dto.minOrderPrice();
        this.quantity = dto.quantity();
        this.expiredAt = dto.expiredAt();
    }

    public void issuedCount() {
        this.issuedCount++;
        if (issuedCount == quantity) {
            this.delete(null);
        }
    }

    public boolean checkIssuedCount() {
        return this.issuedCount >= this.quantity;
    }

    public boolean isAlreadyIssued() {
        return issuedCount > 0;
    }

    public boolean checkMinOrderPrice(Integer minOrderPrice) {
        return this.minOrderPrice > minOrderPrice;

    }
}
