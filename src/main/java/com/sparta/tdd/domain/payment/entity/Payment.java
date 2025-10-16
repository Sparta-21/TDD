package com.sparta.tdd.domain.payment.entity;

import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.payment.enums.CardCompany;
import com.sparta.tdd.domain.payment.enums.PaymentStatus;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.global.exception.BusinessException;
import com.sparta.tdd.global.exception.ErrorCode;
import com.sparta.tdd.global.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "p_payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id", nullable = false)
    private UUID id;

    @Column(name = "number", nullable = false, length = 50)
    private String number;

    @Column(name = "amount", nullable = false, precision = 8)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_company", nullable = false, length = 20)
    private CardCompany cardCompany;

    @Column(name = "card_number", nullable = false, length = 20)
    private String cardNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Builder
    public Payment(String number, Long amount, CardCompany cardCompany, String cardNumber, PaymentStatus status,
        User user, Order order) {
        this.number = number;
        this.amount = amount;
        this.cardCompany = cardCompany;
        this.cardNumber = cardNumber;
        this.status = status == null ? PaymentStatus.PENDING : status;
        this.user = user;
        this.order = order;
    }

    public void approve() {
        validateStatusTransition(PaymentStatus.COMPLETED);
        this.status = PaymentStatus.COMPLETED;
        this.processedAt = LocalDateTime.now();
    }

    public void cancel() {
        validateStatusTransition(PaymentStatus.CANCELLED);
        this.status = PaymentStatus.CANCELLED;
        this.processedAt = LocalDateTime.now();
    }

    public void fail() {
        validateStatusTransition(PaymentStatus.FAILED);
        this.status = PaymentStatus.FAILED;
        this.processedAt = LocalDateTime.now();
    }

    private void validateStatusTransition(PaymentStatus newStatus) {
        if (!newStatus.canTransitionFrom(this.status)) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_REQUEST);
        }
    }
}
