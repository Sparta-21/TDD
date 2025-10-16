package com.sparta.tdd.domain.coupon.repository;

import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.tdd.domain.coupon.entity.QUserCoupon;
import com.sparta.tdd.domain.coupon.entity.UserCoupon;
import com.sparta.tdd.domain.coupon.enums.CouponStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class UserCouponRepositoryCustomImpl implements UserCouponRepositoryCustom {

    private final JPAQueryFactory query;

    @Override
    public List<UserCoupon> findAllByUserId(Long userId) {
        QUserCoupon uc = QUserCoupon.userCoupon;

        List<UserCoupon> coupons = query.selectFrom(uc)
            .where(uc.user.id.eq(userId))
            .orderBy(
                new CaseBuilder()
                    .when(uc.couponStatus.eq(CouponStatus.ACTIVE)).then(1)
                    .otherwise(2).asc(),
                uc.createdAt.desc()
            )
            .fetch();
        return coupons;
    }

}
