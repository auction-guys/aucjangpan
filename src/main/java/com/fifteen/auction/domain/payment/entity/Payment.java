package com.fifteen.auction.domain.payment.entity;

import com.fifteen.auction.domain.order.entity.Order;
import com.fifteen.auction.domain.payment.dto.response.PaymentResponse;
import com.fifteen.auction.domain.payment.enums.PaymentStatus;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
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
    private String mId;
    private String paymentKey;
    private String paymentMethod = "Pending";
    private Long amount;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.READY;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    public Payment(PaymentResponse response, Order order) {
        this.mId = response.getMId();
        this.paymentKey = response.getPaymentKey();
        this.paymentMethod = response.getMethod();
        this.amount = response.getCard().getAmount();
        this.status = response.getStatus();
        this.requestedAt = response.getRequestedAt();
        this.approvedAt = response.getApprovedAt();
        this.order = order;
    }


    public void validateOwner(Long userId) {
        if (!this.order.getUser().getId().equals(userId)) {
            throw new ClientException(ErrorCode.PAYMENT_ACCESS_DENIED);
        }
    }

    public void cancel() {
        this.status = PaymentStatus.CANCELED;
    }
}
