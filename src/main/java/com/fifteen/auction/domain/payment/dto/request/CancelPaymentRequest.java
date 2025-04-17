package com.fifteen.auction.domain.payment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CancelPaymentRequest {
    @NotNull
    private String cancelReason;

    public CancelPaymentRequest(String cancelReason) {
        this.cancelReason = cancelReason;
    }
}
