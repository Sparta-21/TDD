package com.sparta.tdd.domain.point.entity;

import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.global.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_point_wallet")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointWallet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "point_wallet_id", nullable = false, updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "balance")
    private Long balance;

    @Builder
    public PointWallet(User user) {
        this.user = user;
        this.balance = 0L;
    }

    public void addBalance(Long earnAmount) {
        this.balance += earnAmount;
    }

    public Long subtractBalance(Long amount) {
        if (this.balance < amount) {
            Long currentBalance = this.balance;
            this.balance = 0L;
            return amount - currentBalance;
        }
        this.balance -= amount;
        return amount;
    }
}
