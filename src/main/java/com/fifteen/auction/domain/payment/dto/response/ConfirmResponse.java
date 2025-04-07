package com.fifteen.auction.domain.payment.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ConfirmResponse {
    private String mid;
    private String paymentKey;
    private String paymentMethod;
    private Long amount;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;

    public ConfirmResponse(String mid, String paymentKey, String paymentMethod, Long amount, LocalDateTime requestedAt, LocalDateTime approvedAt) {
        this.mid = mid;
        this.paymentKey = paymentKey;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.requestedAt = requestedAt;
        this.approvedAt = approvedAt;
    }
}
