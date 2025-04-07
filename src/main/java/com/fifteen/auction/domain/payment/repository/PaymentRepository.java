package com.fifteen.auction.domain.payment.repository;

import com.fifteen.auction.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
