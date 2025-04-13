package com.fifteen.auction.domain.payment.dto.response;

import lombok.Getter;
import org.json.simple.JSONObject;

import java.time.LocalDateTime;

@Getter
public class ConfirmResponse {
    private String mid;
    private String paymentKey;
    private String paymentMethod;
    private Long amount;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;

    public ConfirmResponse(JSONObject jsonObject) {
        this.mid = jsonObject.get("mid").toString();
        this.paymentKey = jsonObject.get("paymentKey").toString();
        this.paymentMethod = jsonObject.get("method").toString();
        this.amount = Long.parseLong(jsonObject.get("amount").toString());
        this.requestedAt = LocalDateTime.parse(jsonObject.get("requestedAt").toString().substring(0, 19));
        this.approvedAt = LocalDateTime.parse(jsonObject.get("approvedAt").toString().substring(0, 19));
    }
}
