package com.sparta.tdd.domain.payment.repository;

import com.sparta.tdd.domain.payment.entity.Payment;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentRepositoryCustom {

    Page<Payment> findPaymentsByUserId(Long userId, String keyword, Pageable pageable);

    Page<Payment> findPaymentsByStoreId(UUID storeId, String keyword, Pageable pageable);

    Optional<Payment> findPaymentDetailById(UUID paymentId);
}
