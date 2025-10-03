package com.sparta.tdd.domain.order.repository;

import com.sparta.tdd.domain.order.entity.Order;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Modifying
    @Query("UPDATE Order o SET o.deletedAt = :deletedAt, o.deletedBy = :deletedBy WHERE o.user.id = :userId AND o.deletedAt IS NULL")
    void bulkSoftDeleteByUserId(
        @Param("userId") Long userId,
        @Param("deletedAt") LocalDateTime deletedAt,
        @Param("deletedBy") Long deletedBy
    );

    @Query("SELECT o.id FROM Order o WHERE o.user.id = :userId AND o.deletedAt IS NULL")
    List<UUID> findOrderIdsByUserIdAndDeletedAtIsNull(Long userId);
}
