package com.sparta.tdd.domain.order.repository;

import com.sparta.tdd.domain.order.entity.Order;
import java.util.Collection;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID>, OrderRepositoryCustom {

    @Modifying
    @Query("UPDATE Order o SET o.deletedAt = :deletedAt, o.deletedBy = :deletedBy WHERE o.user.id = :userId AND o.deletedAt IS NULL")
    void bulkSoftDeleteByUserId(
            @Param("userId") Long userId,
            @Param("deletedAt") LocalDateTime deletedAt,
            @Param("deletedBy") Long deletedBy
    );

    @Query("SELECT o.id FROM Order o WHERE o.user.id = :userId AND o.deletedAt IS NULL")
    List<UUID> findOrderIdsByUserIdAndDeletedAtIsNull(Long userId);

    @Query("""
          select distinct o
          from Order o
          left join fetch o.user
          left join fetch o.store
          left join fetch o.payment
          left join fetch o.orderMenuList om
          left join fetch om.menu m
          where o.id in :ids
        """)
    List<Order> findDetailsByIdIn(Collection<UUID> ids);

    @Query("""
        select o
        from Order o
        join fetch o.store s
        where o.id = :orderId
        and s.user.id = :userId
        """)
    Optional<Order> findOrderByIdAndStoreUserId(UUID orderId, Long userId);
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.deletedAt IS NULL")
    Page<Order> findOrdersByUserIdAndNotDeleted(@Param("userId") Long userId, Pageable pageable);
}