package com.fifteen.auction.domain.payment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PaymentRequest {
    @NotNull
    private String paymentKey;
    @NotNull
    private String orderId;
    @NotNull
    private Long amount;


    public PaymentRequest(String orderId, Long amount, String paymentKey) {
        this.orderId = orderId;
        this.amount = amount;
        this.paymentKey = paymentKey;
    }
}