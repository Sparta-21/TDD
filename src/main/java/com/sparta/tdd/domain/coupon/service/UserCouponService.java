package com.sparta.tdd.domain.coupon.service;

import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.tdd.domain.coupon.dto.UserCouponResponseDto;
import com.sparta.tdd.domain.coupon.entity.Coupon;
import com.sparta.tdd.domain.coupon.entity.QUserCoupon;
import com.sparta.tdd.domain.coupon.entity.UserCoupon;
import com.sparta.tdd.domain.coupon.enums.Status;
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
    private final JPAQueryFactory query;

    public List<UserCouponResponseDto> getMyCoupons(Long userId) {
        QUserCoupon uc = QUserCoupon.userCoupon;

        List<UserCoupon> coupons = query.selectFrom(uc)
            .where(uc.user.id.eq(userId))
            .orderBy(
                new CaseBuilder()
                    .when(uc.status.eq(Status.ACTIVE)).then(1)
                    .otherwise(2).asc(),
                uc.createdAt.desc()
            )
            .fetch();

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
        userCouponRepository.save(userCoupon);
        coupon.issuedCount();

        return UserCouponResponseDto.from(userCoupon);
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
