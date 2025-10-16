package com.sparta.tdd.domain.coupon.service;

import com.sparta.tdd.domain.coupon.dto.UserCouponResponseDto;
import com.sparta.tdd.domain.coupon.entity.Coupon;
import com.sparta.tdd.domain.coupon.entity.UserCoupon;
import com.sparta.tdd.domain.coupon.repository.CouponRepository;
import com.sparta.tdd.domain.coupon.repository.UserCouponRepository;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.repository.UserRepository;
import com.sparta.tdd.global.exception.BusinessException;
import com.sparta.tdd.global.exception.ErrorCode;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserCouponService {

    private final UserCouponRepository userCouponRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;

    public List<UserCouponResponseDto> getMyCoupons(Long userId) {

        List<UserCoupon> coupons = userCouponRepository.findAllByUserId(userId);

        return coupons.stream()
            .map(UserCouponResponseDto::from)
            .toList();
    }

    @Transactional
    public UserCouponResponseDto createUserCoupon(UUID couponId, Long userId) {
        User user = findUser(userId);
        Coupon coupon = findCoupon(couponId);

        UserCoupon userCoupon = UserCoupon.builder()
            .user(user)
            .coupon(coupon)
            .build();

        if (coupon.checkIssuedCount()) {
            throw new BusinessException(ErrorCode.COUPON_ALL_SOLD_OUT);
        }

        userCouponRepository.save(userCoupon);
        coupon.issuedCount();

        return UserCouponResponseDto.from(userCoupon);
    }

    @Transactional
    public Integer useUserCoupon(Long userId, UUID userCouponId, Integer minOrderPrice) {
        UserCoupon userCoupon = findUserCoupon(userCouponId);
        Coupon coupon = userCoupon.getCoupon();

        if (coupon.checkMinOrderPrice(minOrderPrice)) {
            throw new BusinessException(ErrorCode.COUPON_MIN_PRICE_INVALID);
        }

        userCoupon.updateStatusUsed();
        return coupon.getDiscountValue();
    }

    private UserCoupon findUserCoupon(UUID userCouponId) {
        return userCouponRepository.findById(userCouponId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_COUPON_NOT_FOUND));
    }

    private Coupon findCoupon(UUID couponId) {
        return couponRepository.findByIdAndDeletedAtIsNull(couponId)
            .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
