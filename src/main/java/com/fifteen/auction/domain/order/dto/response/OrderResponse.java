package com.fifteen.auction.domain.order.dto.response;

import com.fifteen.auction.domain.order.enums.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class OrderResponse {
    private String name;
    private String orderId;
    private String address;
    private String paymentType;
    private String productName;
    private String amount;
    private OrderStatus status;
    private LocalDate orderedDate;

    @Builder
    public OrderResponse(String name, String orderId, String address, String paymentType, String productName, String amount, OrderStatus status, LocalDate orderedDate) {
        this.name = name;
        this.orderId = orderId;
        this.address = address;
        this.paymentType = paymentType;
        this.productName = productName;
        this.amount = amount;
        this.status = status;
        this.orderedDate = orderedDate;
    }
}
