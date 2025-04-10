package com.fifteen.auction.domain.payment.dto.request;

import lombok.Getter;

@Getter
public class CancelPaymentRequest {
    private String reason;

    public CancelPaymentRequest(String reason) {
        this.reason = reason;
    }
}
