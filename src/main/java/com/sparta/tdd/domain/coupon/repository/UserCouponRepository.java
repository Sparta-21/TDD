package com.sparta.tdd.domain.coupon.repository;

import com.sparta.tdd.domain.coupon.entity.UserCoupon;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserCouponRepository extends JpaRepository<UserCoupon, UUID>,
    UserCouponRepositoryCustom {

    List<UserCoupon> findAllByUserId(Long userId);

    @Query("update UserCoupon uc set uc.couponStatus = 'EXPIRED' where uc.couponStatus = 'ACTIVE' and uc.coupon.expiredAt <= :now")
    int expireByCouponExpiredAt(@Param("now") LocalDateTime now);

    @Query("update UserCoupon uc set uc.deletedAt = :now, uc.deletedBy = null where uc.couponStatus = 'EXPIRED' and DATE(uc.coupon.expiredAt) <= DATE(:sevenDaysAgo)")
    void deleteExpiredUserCoupon(@Param("now") LocalDateTime now,
        @Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);
}
