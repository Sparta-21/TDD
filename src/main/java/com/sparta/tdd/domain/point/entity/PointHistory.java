package com.sparta.tdd.domain.point.entity;

import com.sparta.tdd.domain.point.dto.PointRequest;
import com.sparta.tdd.domain.point.enums.PointType;
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
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_point_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "point_history_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_wallet_id")
    private PointWallet wallet;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "amount")
    private Long amount;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private PointType type;

    @Column(name = "description")
    private String description;

    @Column(name = "expire_at")
    private LocalDateTime expireAt;

    @Builder
    private PointHistory(PointWallet wallet, UUID referenceId, Long amount,
        PointType type, String description, LocalDateTime expireAt) {
        this.wallet = wallet;
        this.referenceId = referenceId;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.expireAt = expireAt;
    }

    public static PointHistory create(PointWallet wallet, PointRequest request,
        LocalDateTime expireAt) {
        return PointHistory.builder()
            .wallet(wallet)
            .referenceId(request.referenceId())
            .amount(request.amount())
            .type(request.type())
            .description(request.description())
            .expireAt(expireAt)
            .build();
    }
}
