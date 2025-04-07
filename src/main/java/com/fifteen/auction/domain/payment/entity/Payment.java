package com.fifteen.auction.domain.payment.entity;

import com.fifteen.auction.domain.order.entity.Order;
import com.fifteen.auction.domain.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String mid;
    private String paymentKey;
    private String paymentMethod;
    private int amount;
    private PaymentStatus status = PaymentStatus.READY;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
}
