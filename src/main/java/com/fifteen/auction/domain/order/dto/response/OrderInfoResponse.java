package com.fifteen.auction.domain.order.dto.response;

import lombok.Getter;

@Getter
public class OrderInfoResponse {
    private String orderId;
    private String orderName;
    private String customerEmail;
    private String customerName;
    private String amount;

    public OrderInfoResponse(String orderId, String orderName, String customerEmail, String customerName, String amount) {
        this.orderId = orderId;
        this.orderName = orderName;
        this.customerEmail = customerEmail;
        this.customerName = customerName;
        this.amount = amount;
    }
}
