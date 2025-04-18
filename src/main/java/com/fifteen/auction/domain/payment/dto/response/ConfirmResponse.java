package com.fifteen.auction.domain.payment.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ConfirmResponse {
    private String mId;
    private String paymentKey;
    private String paymentMethod;
    private Long amount;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;

    public ConfirmResponse(PaymentResponse response) {
        this.mId = response.getMId();
        this.paymentKey = response.getPaymentKey();
        this.paymentMethod = response.getMethod();
        this.amount = response.getCard().getAmount();
        this.requestedAt = response.getRequestedAt().toLocalDateTime();
        this.approvedAt = response.getApprovedAt().toLocalDateTime();
    }
}
