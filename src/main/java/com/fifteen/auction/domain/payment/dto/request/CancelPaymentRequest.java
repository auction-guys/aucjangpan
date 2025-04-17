package com.fifteen.auction.domain.payment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CancelPaymentRequest {
    @NotNull
    private String cancelReason;

    public CancelPaymentRequest(String cancelReason) {
        this.cancelReason = cancelReason;
    }
}
