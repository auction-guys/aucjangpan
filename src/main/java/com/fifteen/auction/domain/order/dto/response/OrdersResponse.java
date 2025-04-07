package com.fifteen.auction.domain.order.dto.response;

import com.fifteen.auction.domain.order.enums.OrderStatus;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class OrdersResponse {
    private String orderId;
    private String productName;
    private String amount;
    private OrderStatus status;
    private LocalDate orderDate;

    public OrdersResponse(String orderId, String productName, String amount, OrderStatus status, LocalDate orderDate) {
        this.orderId = orderId;
        this.productName = productName;
        this.amount = amount;
        this.status = status;
        this.orderDate = orderDate;
    }
}
