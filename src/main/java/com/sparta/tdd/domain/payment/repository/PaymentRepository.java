package com.sparta.tdd.domain.payment.repository;

import com.sparta.tdd.domain.payment.entity.Payment;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    @Modifying
    @Query("UPDATE Payment p SET p.deletedAt = :deletedAt, p.deletedBy = :deletedBy WHERE p.user.id = :userId AND p.deletedAt IS NULL")
    void bulkSoftDeleteByUserId(
        @Param("userId") Long userId,
        @Param("deletedAt") LocalDateTime deletedAt,
        @Param("deletedBy") Long deletedBy
    );
}
