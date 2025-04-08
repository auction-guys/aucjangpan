package com.fifteen.auction.domain.order.controller;

import com.fifteen.auction.domain.order.dto.request.CreateOrderRequest;
import com.fifteen.auction.domain.order.dto.response.OrderInfoResponse;
import com.fifteen.auction.domain.order.dto.response.OrderResponse;
import com.fifteen.auction.domain.order.dto.response.OrdersResponse;
import com.fifteen.auction.domain.order.service.OrderService;
import com.fifteen.auction.global.dto.PageCond;
import com.fifteen.auction.global.dto.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/api/v1/orders/{orderId}/payment")
    public ResponseEntity<OrderInfoResponse> getOrderInfo(
            @RequestParam Long orderId){
        return ResponseEntity.ok(orderService.getOrderInfo(orderId));
    }

    @PostMapping("api/v1/orders")
    public ResponseEntity<Void> createOrder(@RequestBody CreateOrderRequest dto){
        orderService.createOrder(dto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("api/v1/orders")
    public ResponseEntity<Response<Page<OrdersResponse>>> findOrders(
            Long loginedId,
            PageCond pageCond){
        Response<Page<OrdersResponse>> response = orderService.findOrders(loginedId, pageCond);

        return ResponseEntity.ok(response);
    }

    @GetMapping("api/v1/orders/{orderId}")
    public ResponseEntity<Response<OrderResponse>> findOrder(
            Long loginedId,
            @PathVariable String orderId){

        return ResponseEntity.ok(Response.of(orderService.findOrder(loginedId, orderId)));
    }
}
