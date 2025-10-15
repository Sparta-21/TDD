package com.sparta.tdd.domain.coupon.repository;

import com.sparta.tdd.domain.coupon.entity.Coupon;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {

    List<Coupon> findAllByStoreIdAndDeletedAtIsNull(UUID storeId);

    List<Coupon> findAllByStoreId(UUID storeId);
}
