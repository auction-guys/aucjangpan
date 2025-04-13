package com.fifteen.auction.domain.payment.dto.request;

import lombok.Getter;

@Getter
public class CancelPaymentRequest {
    private String cancelReason;

    public CancelPaymentRequest(String cancelReason) {
        this.cancelReason = cancelReason;
    }
}
