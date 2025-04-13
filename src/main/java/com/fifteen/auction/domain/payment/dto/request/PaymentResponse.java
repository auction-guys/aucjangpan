package com.fifteen.auction.domain.payment.dto.request;

import com.fifteen.auction.domain.order.entity.Order;
import lombok.Getter;
import org.json.simple.JSONObject;

@Getter
public class PaymentResponse {
    private String paymentKey;
    private Long amount;
    private String orderId;
    private JSONObject jsonObject;
    private Order order;

    public PaymentResponse(JSONObject jsonObject, Order order) {
        JSONObject card = (JSONObject) jsonObject.get("card");
        this.paymentKey = jsonObject.get("paymentKey").toString();
        this.amount = Long.parseLong(card.get("amount").toString());
        this.orderId = jsonObject.get("orderId").toString();
        this.jsonObject = jsonObject;

        this.order = order;
    }
}
