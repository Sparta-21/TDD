package com.sparta.tdd.domain.orderMenu.repository;

import com.sparta.tdd.domain.orderMenu.entity.OrderMenu;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderMenuRepository extends JpaRepository<OrderMenu, UUID> {

    @Modifying
    @Query("UPDATE OrderMenu om SET om.deletedAt = :deletedAt, om.deletedBy = :deletedBy WHERE om.order.id IN :storeIds AND om.deletedAt IS NULL")
    void bulkSoftDeleteByOrderIds(
        @Param("storeIds") List<UUID> orderIds,
        @Param("deletedAt") LocalDateTime deletedAt,
        @Param("deletedBy") Long deletedBy
    );
}
