package com.sparta.tdd.domain.coupon.repository;

import com.sparta.tdd.domain.coupon.entity.Coupon;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {

    List<Coupon> findAllByStoreIdAndDeletedAtIsNull(UUID storeId);

    Optional<Coupon> findByIdAndDeletedAtIsNull(UUID id);

    @Modifying(clearAutomatically = true)
    @Query("update Coupon c set c.deletedAt = :now, c.deletedBy = null where c.deletedAt is null and c.expiredAt <= :now")
    int softDeleteExpiredCoupons(@Param("now") LocalDateTime now);

}
