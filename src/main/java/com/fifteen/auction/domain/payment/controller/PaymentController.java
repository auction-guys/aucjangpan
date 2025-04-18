package com.fifteen.auction.domain.payment.controller;

import com.fifteen.auction.domain.payment.dto.request.CancelPaymentRequest;
import com.fifteen.auction.domain.payment.dto.request.PaymentRequest;
import com.fifteen.auction.domain.payment.dto.response.ConfirmResponse;
import com.fifteen.auction.domain.payment.dto.response.FindPaymentResponse;
import com.fifteen.auction.domain.payment.service.PaymentService;
import com.fifteen.auction.global.dto.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/api/v1/payment/cancelReason")
    public ResponseEntity<Void> getCancelReason(
            @RequestParam String code,
            @RequestParam String message,
            @RequestParam String orderId) {

        log.info("결제 실패 코드 : {}, 메시지 : {}, 주문 Id : {}", code, message, orderId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/payments/confirm")
    public ResponseEntity<Response<ConfirmResponse>> confirmPayment(
//            @AuthenticationPrincipal AuthUser authUser,
            @ModelAttribute PaymentRequest paymentRequest) {
        Long currentUserId = 2L;// 테스트용

        return ResponseEntity.ok(Response.of(paymentService.confirm(paymentRequest, currentUserId)));
    }

    @PostMapping("/api/v1/payments/{paymentKey}/cancel")
    public ResponseEntity<Void> cancelPayment(
//            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable String paymentKey,
            @RequestBody CancelPaymentRequest dto) {

        Long currentUserId = 2L;
        paymentService.cancelPaymentByUser(paymentKey, dto, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/payments/{paymentKey}")
    public ResponseEntity<Response<FindPaymentResponse>> findPayment(
//            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable String paymentKey) {

        Long currentUserId = 2L;
        return ResponseEntity.ok(Response.of(paymentService.findPayment(paymentKey, currentUserId)));
    }
}