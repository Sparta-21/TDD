package com.sparta.tdd.domain.coupon.service;

import com.sparta.tdd.domain.coupon.dto.CouponRequestDto;
import com.sparta.tdd.domain.coupon.dto.CouponResponseDto;
import com.sparta.tdd.domain.coupon.entity.Coupon;
import com.sparta.tdd.domain.coupon.repository.CouponRepository;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.store.repository.StoreRepository;
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
public class CouponService {

    private final CouponRepository couponRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    public List<CouponResponseDto> getStoreCoupons(UUID storeId) {
        List<Coupon> coupons = couponRepository.findAllByStoreIdAndDeletedAtIsNull(storeId);

        return coupons.stream()
            .map(CouponResponseDto::from)
            .toList();
    }


    @Transactional
    public CouponResponseDto createStoreCoupon(UUID storeId, CouponRequestDto dto, Long userId) {
        User user = findUser(userId);
        Store store = findStore(storeId);
        validateUserOnCoupon(user, store);

        if (dto.masterScope()) {
            throw new BusinessException(ErrorCode.COUPON_BAD_REQUEST);
        }

        Coupon coupon = Coupon.builder()
            .dto(dto)
            .store(store)
            .build();
        couponRepository.save(coupon);

        return CouponResponseDto.from(coupon);
    }

    @Transactional
    public CouponResponseDto createMasterCoupon(CouponRequestDto dto) {
        if (dto.storeScope()) {
            throw new BusinessException(ErrorCode.COUPON_BAD_REQUEST);
        }

        Coupon coupon = Coupon.builder()
            .dto(dto)
            .store(null)
            .build();
        couponRepository.save(coupon);

        return CouponResponseDto.from(coupon);
    }

    @Transactional
    public void updateCoupon(UUID storeId, UUID couponId, CouponRequestDto dto, Long userId) {
        Coupon coupon = findCoupon(couponId);
        if (coupon.isAlreadyIssued()) {
            throw new BusinessException(ErrorCode.COUPON_ALREADY_ISSUED);
        }

        User user = findUser(userId);
        Store store = findStore(storeId);
        validateUserOnCoupon(user, store);

        coupon.update(dto);
    }

    @Transactional
    public void deleteCoupon(UUID storeId, UUID couponId, Long userId) {
        User user = findUser(userId);
        Store store = findStore(storeId);
        validateUserOnCoupon(user, store);

        Coupon coupon = findCoupon(couponId);
        coupon.delete(userId);
    }

    private Coupon findCoupon(UUID couponId) {
        return couponRepository.findByIdAndDeletedAtIsNull(couponId)
            .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private Store findStore(UUID storeId) {
        return storeRepository.findByStoreIdAndNotDeleted(storeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
    }

    private void validateUserOnCoupon(User user, Store store) {
        if (!store.isOwner(user)) {
            throw new BusinessException(ErrorCode.COUPON_PERMISSION_DENIED);
        }
    }

}
