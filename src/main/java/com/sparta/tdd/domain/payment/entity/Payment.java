package com.sparta.tdd.domain.payment.entity;

import com.sparta.tdd.domain.payment.enums.CardCompany;
import com.sparta.tdd.domain.payment.enums.PaymentStatus;
import com.sparta.tdd.global.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "p_payment")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
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

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Builder.Default
    @Column(name = "is_hidden", nullable = false)
    private boolean isHidden = false;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
}
