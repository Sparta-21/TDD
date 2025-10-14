package com.sparta.tdd.domain.coupon.repository;

import com.sparta.tdd.domain.coupon.entity.Coupon;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {

}
