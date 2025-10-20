package com.sparta.tdd.domain.payment.repository;

import com.sparta.tdd.domain.payment.entity.Payment;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, UUID>,
    PaymentRepositoryCustom {

    @Modifying
    @Query("UPDATE Payment p SET p.deletedAt = :deletedAt, p.deletedBy = :deletedBy WHERE p.user.id = :userId AND p.deletedAt IS NULL")
    void bulkSoftDeleteByUserId(
        @Param("userId") Long userId,
        @Param("deletedAt") LocalDateTime deletedAt,
        @Param("deletedBy") Long deletedBy
    );

    @SuppressWarnings("NullableProblems")
    @EntityGraph(attributePaths = {"order", "user"})
    Optional<Payment> findById(UUID id);
}
