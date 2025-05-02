package com.fifteen.auction.domain.payment.dto.response;

import com.fifteen.auction.domain.payment.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String method;
    private String paymentKey;
    private PaymentCardResponse card;
    private PaymentStatus status;
    private String orderId;
    private OffsetDateTime requestedAt;
    private OffsetDateTime approvedAt;
}
