package com.sparta.tdd.domain.coupon.repository;

import com.sparta.tdd.domain.coupon.entity.UserCoupon;
import java.util.List;

public interface UserCouponRepositoryCustom {

    List<UserCoupon> findAllByUserId(Long userId);
}
