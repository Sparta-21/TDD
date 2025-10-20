package com.sparta.tdd.domain.payment.service;

import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.order.enums.OrderStatus;
import com.sparta.tdd.domain.payment.entity.Payment;
import com.sparta.tdd.domain.payment.enums.PaymentStatus;
import com.sparta.tdd.global.exception.BusinessException;
import com.sparta.tdd.global.exception.ErrorCode;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentResultProcessService {

    public void processPaymentResult(Payment payment) {
        Order order = payment.getOrder();
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        PaymentStatus status = payment.getStatus();

        switch (status) {
            case COMPLETED -> processApproved(payment, order);
            case CANCELLED -> processCancelled(payment, order);
            case FAILED -> processFailed(payment, order);
            case PENDING -> {
            }
        }
    }

    private void processApproved(Payment payment, Order order) {
        // 이미 배달완료된 주문이면 상태 변경 스킵
        if (order.getOrderStatus() == OrderStatus.DELIVERED) {
            return;
        }

        // 주문 상태 변경 (PENDING -> DELIVERED)
        order.nextStatus();
    }

    private void processCancelled(Payment payment, Order order) {
        if (LocalDateTime.now().minusMinutes(5).isAfter(payment.getCreatedAt())) {
            throw new BusinessException(ErrorCode.PAYMENT_CANCEL_TIME_EXPIRED);
        }
        // 주문 상태를 PENDING으로 복구
        order.changeOrderStatus(OrderStatus.PENDING);

        // 환불처리는 진행 된 것으로 가정하겠습니다.
    }

    private void processFailed(Payment payment, Order order) {
        // 주문 상태를 PENDING으로 유지 (재결제 가능하도록)
        order.changeOrderStatus(OrderStatus.PENDING);
    }
}
