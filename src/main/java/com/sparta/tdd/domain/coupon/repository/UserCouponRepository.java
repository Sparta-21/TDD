package com.sparta.tdd.domain.coupon.repository;

import com.sparta.tdd.domain.coupon.entity.UserCoupon;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCouponRepository extends JpaRepository<UserCoupon, UUID> {

    List<UserCoupon> findAllByUserId(Long userId);
}
