package com.fifteen.auction.domain.payment.dto.request;

import lombok.Getter;

@Getter
public class PaymentRequest {
    private String orderId;
    private Long amount;
    private String paymentKey;

    public PaymentRequest(String orderId, Long amount, String paymentKey) {
        this.orderId = orderId;
        this.amount = amount;
        this.paymentKey = paymentKey;
    }
}