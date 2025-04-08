package com.fifteen.auction.domain.payment.repository;

import com.fifteen.auction.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(long orderId);

    Optional<Payment> findByPaymentKey(String paymentKey);
}
