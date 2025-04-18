package com.fifteen.auction.domain.payment.dto.response;

import com.fifteen.auction.domain.payment.enums.PaymentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
public class PaymentResponse {
    private String mId;
    private String method;
    private String paymentKey;
    private PaymentCardResponse card;
    private PaymentStatus status;
    private String orderId;
    private OffsetDateTime requestedAt;
    private OffsetDateTime approvedAt;
}
