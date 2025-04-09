package com.fifteen.auction.domain.order.controller;

import com.fifteen.auction.domain.order.dto.response.OrderInfoResponse;
import com.fifteen.auction.domain.order.dto.response.OrderResponse;
import com.fifteen.auction.domain.order.dto.response.OrdersResponse;
import com.fifteen.auction.domain.order.service.OrderService;
import com.fifteen.auction.domain.user.auth.entity.AuthUser;
import com.fifteen.auction.global.dto.PageCond;
import com.fifteen.auction.global.dto.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class OrderController {

    // 회원 인증 기능 생기면 currentUserId를 그걸로 대체

    private final OrderService orderService;

    @GetMapping("/api/v1/orders/{orderId}/payment")
    public ResponseEntity<OrderInfoResponse> getOrderInfo(
            @PathVariable String orderId) {
        return ResponseEntity.ok(orderService.getOrderInfo(orderId));
    }

    @PostMapping("api/v1/orders")
    public ResponseEntity<Void> createOrder(
//            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam Long auctionId) {
        Long currentUserId = 2L;
        orderService.createOrder(currentUserId, auctionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("api/v1/orders")
    public ResponseEntity<Response<Page<OrdersResponse>>> findOrders(
//            @AuthenticationPrincipal AuthUser authUser,
            @ModelAttribute PageCond pageCond) {
        Long currentUserId = 2L;
        Response<Page<OrdersResponse>> response = orderService.findOrders(currentUserId, pageCond);

        return ResponseEntity.ok(response);
    }

    @GetMapping("api/v1/orders/{orderId}")
    public ResponseEntity<Response<OrderResponse>> findOrder(
//            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable String orderId) {
        Long currentUserId = 2L;
        return ResponseEntity.ok(Response.of(orderService.findOrder(currentUserId, orderId)));
    }

    @DeleteMapping("api/v1/orders/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(
//            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable String orderId) {
        Long currentUserId = 2L;
        orderService.cancelOrder(currentUserId, orderId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("api/v1/orders/{orderId}/confirmed")
    public ResponseEntity<Void> confirmOrder(
//            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable String orderId) {
        Long currentUserId = 2L;
        orderService.confirmOrder(currentUserId, orderId);

        return ResponseEntity.noContent().build();
    }
}
