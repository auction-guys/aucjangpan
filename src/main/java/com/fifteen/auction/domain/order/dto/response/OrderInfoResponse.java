package com.fifteen.auction.domain.order.dto.response;

import com.fifteen.auction.domain.order.entity.Order;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderInfoResponse {
    private String orderId;
    private String orderName;
    private String customerEmail;
    private String customerName;
    private String amount;

    public static OrderInfoResponse from(Order order){
        return new OrderInfoResponse(
                order.getId(),
                order.getAuction().getProduct().getName(),
                order.getAuction().getProduct().getSeller().getEmail(),
                order.getAuction().getProduct().getSeller().getName(),
                order.getAuction().getWinPrice().toString()
        );
    }
}
