package com.sparta.tdd.domain.point.service;

import com.sparta.tdd.domain.point.dto.PointRequest;
import com.sparta.tdd.domain.point.entity.PointHistory;
import com.sparta.tdd.domain.point.entity.PointWallet;
import com.sparta.tdd.domain.point.enums.PointType;
import com.sparta.tdd.domain.point.repository.PointHistoryRepository;
import com.sparta.tdd.domain.point.repository.PointWalletRepository;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.global.exception.BusinessException;
import com.sparta.tdd.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {

    private final PointWalletRepository walletRepository;
    private final PointHistoryRepository historyRepository;

    @Transactional
    public void earnPoints(PointRequest request) {
        if (isDuplicateEarn(request.referenceId(), request.type())) {
            return;
        }

        PointWallet wallet = getOrCreateWallet(request.user());
        wallet.addBalance(request.amount());

        PointHistory pointHistory = PointHistory.create(wallet, request,
            LocalDateTime.now().plusYears(1));

        historyRepository.save(pointHistory);
    }

    @Transactional
    public void losePoints(User user, UUID paymentId) {
        PointWallet wallet = findWalletById(user.getId());
        PointHistory pointHistory = findPointHistory(paymentId);
        Long usedAmount = wallet.subtractBalance(pointHistory.getAmount());

        PointHistory cancelledHistory = PointHistory.builder()
            .wallet(wallet)
            .referenceId(paymentId)
            .amount(usedAmount)
            .type(PointType.PAYMENT_CANCELLED)
            .description("결제 취소 (결제번호: " + paymentId + ")")
            .expireAt(LocalDateTime.now())
            .build();

        historyRepository.save(cancelledHistory);
    }

    private PointWallet findWalletById(Long userId) {
        return walletRepository.findByUserId(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private PointHistory findPointHistory(UUID paymentId) {
        return historyRepository.findByReferenceIdAndTypeAndDeletedAtIsNull(paymentId,
                PointType.PAYMENT_EARNED)
            .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
    }


    private PointWallet getOrCreateWallet(User user) {
        return walletRepository.findByUserId(user.getId())
            .orElseGet(() -> walletRepository.save(PointWallet.builder()
                .user(user)
                .build()));
    }

    private boolean isDuplicateEarn(UUID referenceId, PointType type) {
        if (referenceId == null || type == null) {
            return false;
        }
        return historyRepository.existsByReferenceIdAndTypeAndDeletedAtIsNull(referenceId, type);
    }
}
