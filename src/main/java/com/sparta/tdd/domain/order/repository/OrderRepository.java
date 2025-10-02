package com.sparta.tdd.domain.order.repository;

import com.sparta.tdd.domain.order.entity.Order;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID>, OrderRepositoryCustom {

//    @Query("""
//            select distinct o
//            from Order o
//            left join fetch o.user u
//            left join fetch o.store s
//            left join fetch o.payment p
//            left join fetch o.orderMenuList om
//            left join fetch om.menu m
//            where o.id = :id
//        """)
//    Optional<Order> findDetailById(UUID id);

    @Query("""
          select o.id
          from Order o
          order by o.createdAt desc
        """)
    Page<UUID> findPageIds(Pageable pageable, Long targetUserId, LocalDateTime start,
        LocalDateTime end, UUID targetStoreId);

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
}
