package com.sparta.tdd.domain.payment.service;

import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.order.repository.OrderRepository;
import com.sparta.tdd.domain.orderMenu.entity.OrderMenu;
import com.sparta.tdd.domain.payment.dto.PaymentDetailResponseDto;
import com.sparta.tdd.domain.payment.dto.PaymentListResponseDto;
import com.sparta.tdd.domain.payment.dto.PaymentRequestDto;
import com.sparta.tdd.domain.payment.dto.UpdatePaymentStatusRequest;
import com.sparta.tdd.domain.payment.entity.Payment;
import com.sparta.tdd.domain.payment.enums.PaymentStatus;
import com.sparta.tdd.domain.payment.repository.PaymentRepository;
import com.sparta.tdd.domain.payment.util.PaymentNumberGenerator;
import com.sparta.tdd.domain.store.repository.StoreRepository;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.repository.UserRepository;
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
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PaymentResultProcessService paymentResultProcessService;

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

        switch (request.status()) {
            case COMPLETED -> payment.approve();
            case CANCELLED -> payment.cancel();
            case FAILED -> payment.fail();
            case PENDING -> throw new BusinessException(ErrorCode.INVALID_PAYMENT_REQUEST);
        }

        // 결제 상태 변경 후 후속 처리 실행
        paymentResultProcessService.processPaymentResult(payment);
    }

    @Transactional
    public void requestPayment(Long userId, PaymentRequestDto request) {
        Order order = findOrder(request.orderId());
        validateRequestPayment(userId, order);

        User user = findUser(userId);

        long totalAmount = 0L;
        for (OrderMenu orderMenu : order.getOrderMenuList()) {
            totalAmount += (long) orderMenu.getPrice() * orderMenu.getQuantity();
        }

        Payment newPayment = Payment.builder()
            .number(PaymentNumberGenerator.generate())
            .amount(totalAmount)
            .cardCompany(request.cardCompany())
            .cardNumber(request.cardNumber())
            .status(PaymentStatus.PENDING)
            .user(user)
            .order(order)
            .build();

        paymentRepository.save(newPayment);
    }

    private boolean isNotUserStore(Long userId, UUID storeId) {
        return storeRepository.existsByIdAndUserIdAndDeletedAtIsNull(storeId, userId);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private Order findOrder(UUID orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    private void validateRequestPayment(Long userId, Order order) {
        if (!order.isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.ORDER_PERMISSION_DENIED);
        }

        if (order.getPayment() != null) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_EXIST);
        }
    }

}

