package com.fifteen.auction.domain.order.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class OrdersResponse {
    private String orderId;
    private String productName;
    private String amount;
    private String status;
    private String orderedDate;

    @Builder
    public OrdersResponse(String orderId, String productName, String amount, String status, String orderedDate) {
        this.orderId = orderId;
        this.productName = productName;
        this.amount = amount;
        this.status = status;
        this.orderedDate = orderedDate;
    }
}
