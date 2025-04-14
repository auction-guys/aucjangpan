package com.fifteen.auction.domain.payment.dto.request;

import com.fifteen.auction.domain.order.entity.Order;
import lombok.Builder;
import lombok.Getter;
import org.json.simple.JSONObject;

@Getter
public class SavePaymentRequset {
    private JSONObject jsonObject;
    private Order order;

    @Builder
    public SavePaymentRequset(JSONObject jsonObject, Order order) {
        this.jsonObject = jsonObject;
        this.order = order;
    }
}
