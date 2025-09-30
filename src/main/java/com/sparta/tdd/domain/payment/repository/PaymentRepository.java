package com.sparta.tdd.domain.payment.repository;

import com.sparta.tdd.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

}
