package com.fifteen.auction.domain.order.controller;

import com.fifteen.auction.domain.order.dto.response.OrderInfoResponse;
import com.fifteen.auction.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/api/v1/orders/{orderId}/payment")
    public ResponseEntity<OrderInfoResponse> getOrderInfo(
            @RequestParam Long orderId){
        return ResponseEntity.ok(orderService.getOrderInfo(orderId));
    }
}
