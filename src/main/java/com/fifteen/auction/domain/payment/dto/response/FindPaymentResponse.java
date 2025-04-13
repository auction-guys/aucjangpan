package com.fifteen.auction.domain.payment.dto.response;

import com.fifteen.auction.domain.payment.entity.Payment;
import lombok.Builder;
import lombok.Getter;

@Getter
public class FindPaymentResponse {
    private String paymentKey;
    private String orderId;
    private String orderName;
    private String status;
    private String requestAt;
    private String approvedAt;
    private String paymentMethod;
    private String amount;

    @Builder
    public FindPaymentResponse(String paymentKey, String orderId, String orderName, String status, String requestAt, String approvedAt, String paymentMethod, String amount) {
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.orderName = orderName;
        this.status = status;
        this.requestAt = requestAt;
        this.approvedAt = approvedAt;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
    }

    public static FindPaymentResponse from(Payment payment) {
        return new FindPaymentResponse(
            payment.getPaymentKey(),
            payment.getOrder().getId().toString(),
            payment.getOrder().getAuction().getProduct().getName(),
            payment.getStatus().toString(),
            payment.getRequestedAt().toString(),
            payment.getApprovedAt().toString(),
            payment.getPaymentMethod(),
            payment.getOrder().getAuction().getWinPrice().toString()
        );
    }
}
