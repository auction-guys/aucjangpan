package com.fifteen.auction.domain.payment.dto.request;

import lombok.Getter;

@Getter
public class CancelPaymentRequest {
    private String resson;

    public CancelPaymentRequest(String resson) {
        this.resson = resson;
    }
}
