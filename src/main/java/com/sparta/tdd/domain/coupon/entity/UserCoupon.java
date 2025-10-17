package com.sparta.tdd.domain.coupon.entity;

import com.sparta.tdd.domain.coupon.enums.CouponStatus;
import com.sparta.tdd.domain.user.entity.User;
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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_user_coupon", uniqueConstraints = {
    @UniqueConstraint(
        name = "unique_coupon",
        columnNames = {"user_id", "coupon_id"}
    )
})
@Getter
@NoArgsConstructor
public class UserCoupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_coupon_id", nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CouponStatus couponStatus = CouponStatus.ACTIVE;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Builder
    public UserCoupon(User user, Coupon coupon) {
        this.user = user;
        this.coupon = coupon;
    }

    public void updateStatusUsed() {
        this.couponStatus = CouponStatus.USED;
        this.usedAt = LocalDateTime.now();
    }
}
