package com.sparta.tdd.domain.coupon.scheduler;

import com.sparta.tdd.domain.coupon.repository.CouponRepository;
import com.sparta.tdd.domain.coupon.repository.UserCouponRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CouponScheduler {

    public final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;

    // userCoupon 만료 처리 및 만료된 coupon soft delete
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expireUserCoupons() {
        LocalDateTime now = LocalDateTime.now();
        userCouponRepository.expireByCouponExpiredAt(now);
        couponRepository.softDeleteExpiredCoupons(now);
    }

    // 만료된 userCoupon 7일 후 soft delete
    @Scheduled(cron = "0 30 0 * * *")
    @Transactional
    public void deleteOldExpiredUserCoupons() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        userCouponRepository.deleteExpiredUserCoupon(now, sevenDaysAgo);
    }
}
