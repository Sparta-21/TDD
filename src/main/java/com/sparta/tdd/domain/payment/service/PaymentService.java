package com.sparta.tdd.domain.payment.service;

import com.sparta.tdd.domain.payment.dto.CreatePaymentRequest;
import com.sparta.tdd.domain.payment.dto.PaymentDetailResponseDto;
import com.sparta.tdd.domain.payment.dto.PaymentListResponseDto;
import com.sparta.tdd.domain.payment.dto.UpdatePaymentStatusRequest;
import com.sparta.tdd.domain.payment.entity.Payment;
import com.sparta.tdd.domain.payment.repository.PaymentRepository;
import com.sparta.tdd.domain.store.repository.StoreRepository;
import com.sparta.tdd.global.exception.BusinessException;
import com.sparta.tdd.global.exception.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final StoreRepository storeRepository;

    public Page<PaymentListResponseDto> getCustomerPaymentHistory(Long userId, Pageable pageable, String keyword) {
        Page<Payment> paymentPage = paymentRepository.findPaymentsByUserId(userId, keyword, pageable);

        return PaymentListResponseDto.from(paymentPage);
    }

    public Page<PaymentListResponseDto> getStorePaymentHistory(
        Long userId, UUID storeId, Pageable pageable, String keyword
    ) {

        if (!isNotUserStore(userId, storeId)) {
            throw new BusinessException(ErrorCode.GET_STORE_PAYMENT_DENIED);
        }

        Page<Payment> paymentPage = paymentRepository.findPaymentsByStoreId(storeId, keyword, pageable);

        return PaymentListResponseDto.from(paymentPage);
    }

    public PaymentDetailResponseDto getPaymentHistoryDetail(UUID paymentId) {
        Payment payment = paymentRepository.findPaymentDetailById(paymentId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        return PaymentDetailResponseDto.from(payment);
    }

    @Transactional
    public void changePaymentStatus(UUID paymentId, UpdatePaymentStatusRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        payment.updateStatus(request.status());
    }

    @Transactional
    public void requestPayment(CreatePaymentRequest request) {
        // 어떻게 요청을 처리할 지 추후 고려
    }

    private boolean isNotUserStore(Long userId, UUID storeId) {
        return storeRepository.existsByIdAndUserIdAndDeletedAtIsNull(storeId, userId);
    }
}

