package com.fifteen.auction.domain.payment.dto.request;

import com.fifteen.auction.domain.order.entity.Order;
import lombok.Getter;
import org.json.simple.JSONObject;

@Getter
public class PaymentResponse {
    private JSONObject jsonObject;
    private Order order;

    public PaymentResponse(JSONObject jsonObject, Order order) {
        this.jsonObject = jsonObject;
        this.order = order;
    }
}
