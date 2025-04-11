package com.fifteen.auction.domain.payment.entity;

import com.fifteen.auction.domain.order.entity.Order;
import com.fifteen.auction.domain.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Builder;
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
    private String paymentMethod = "Pending";
    private String cardNumber;
    private Long amount;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.READY;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Builder
    public Payment(String mid, String paymentKey, String paymentMethod, String cardNumber, Long amount, PaymentStatus status, LocalDateTime requestedAt, LocalDateTime approvedAt, Order order) {
        this.mid = mid;
        this.paymentKey = paymentKey;
        this.paymentMethod = paymentMethod;
        this.cardNumber = cardNumber;
        this.amount = amount;
        this.status = status;
        this.requestedAt = requestedAt;
        this.approvedAt = approvedAt;
        this.order = order;
    }

    public void cancel() {
        this.status = PaymentStatus.CANCELED;
    }
}
