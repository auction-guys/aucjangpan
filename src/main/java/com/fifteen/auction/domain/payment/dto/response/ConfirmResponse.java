package com.fifteen.auction.domain.payment.dto.response;

import com.fifteen.auction.domain.payment.entity.Payment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ConfirmResponse {
    private String paymentKey;
    private String paymentMethod;
    private Long amount;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;

    public ConfirmResponse(PaymentResponse response) {
        this.paymentKey = response.getPaymentKey();
        this.paymentMethod = response.getMethod();
        this.amount = response.getCard().getAmount();
        this.requestedAt = response.getRequestedAt().toLocalDateTime();
        this.approvedAt = response.getApprovedAt().toLocalDateTime();
    }
}
