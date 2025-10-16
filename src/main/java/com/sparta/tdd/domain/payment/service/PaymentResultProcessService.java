package com.sparta.tdd.domain.payment.service;

import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.order.enums.OrderStatus;
import com.sparta.tdd.domain.payment.entity.Payment;
import com.sparta.tdd.domain.payment.enums.PaymentStatus;
import com.sparta.tdd.global.exception.BusinessException;
import com.sparta.tdd.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
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
            case PENDING -> log.warn("PENDING 상태에서는 후속 처리가 필요하지 않습니다. paymentId: {}", payment.getId());
        }
    }

    private void processApproved(Payment payment, Order order) {
        log.info("결제 승인 처리 시작 - paymentId: {}, orderId: {}", payment.getId(), order.getId());

        // 이미 배달완료된 주문이면 상태 변경 스킵
        if (order.getOrderStatus() == OrderStatus.DELIVERED) {
            return;
        }

        // 주문 상태 변경 (PENDING -> DELIVERED)
        order.nextStatus();

        log.info("결제 승인 처리 완료 - 주문 상태: {}", order.getOrderStatus());

        // TODO: 포인트 적립
    }

    private void processCancelled(Payment payment, Order order) {
        log.info("결제 취소 처리 시작 - paymentId: {}, orderId: {}", payment.getId(), order.getId());

        // 주문 상태를 PENDING으로 복구
        order.changeOrderStatus(OrderStatus.PENDING);

        log.info("결제 취소 처리 완료 - 주문 상태: {}", order.getOrderStatus());

        // 환불처리는 진행 된 것으로 가정하겠습니다.

        // TODO 재고 복구, 적립된 포인트 회수 등
    }

    private void processFailed(Payment payment, Order order) {
        log.info("결제 실패 처리 시작 - paymentId: {}, orderId: {}", payment.getId(), order.getId());

        // 주문 상태를 PENDING으로 유지 (재결제 가능하도록)
        order.changeOrderStatus(OrderStatus.PENDING);

        log.info("결제 실패 처리 완료 - 주문 상태: {}", order.getOrderStatus());
    }
}
