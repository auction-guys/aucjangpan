package com.fifteen.auction.domain.order.dto.response;

import com.fifteen.auction.domain.order.enums.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class OrderResponse {
    private String name;
    private String orderId;
    private String address;
    private String productName;
    private String amount;
    private OrderStatus status;
    private LocalDate orderedDate;

    @Builder
    public OrderResponse(String name, String orderId, String address, String productName, String amount, String status, LocalDateTime orderedDate) {
        this.name = name;
        this.orderId = orderId;
        this.address = address;
        this.productName = productName;
        this.amount = amount;
        this.status = OrderStatus.valueOf(status);
        this.orderedDate = orderedDate.toLocalDate();
    }
}
