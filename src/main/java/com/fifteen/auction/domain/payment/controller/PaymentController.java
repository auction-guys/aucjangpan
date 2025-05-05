package com.fifteen.auction.domain.payment.controller;

import com.fifteen.auction.domain.payment.dto.request.CancelPaymentRequest;
import com.fifteen.auction.domain.payment.dto.request.PaymentRequest;
import com.fifteen.auction.domain.payment.dto.response.ConfirmResponse;
import com.fifteen.auction.domain.payment.dto.response.FindPaymentResponse;
import com.fifteen.auction.domain.payment.dto.response.PaymentResponse;
import com.fifteen.auction.domain.payment.service.PaymentService;
import com.fifteen.auction.domain.payment.util.lock.PaymentLockFacade;
import com.fifteen.auction.domain.payment.util.toss.TossWebhookVerifier;
import com.fifteen.auction.domain.user.auth.entity.AuthUser;
import com.fifteen.auction.global.dto.Response;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ServerException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.Executor;

@Slf4j
@RestController
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentLockFacade paymentLockFacade;
    private final Executor executor;

    public PaymentController(
            PaymentService paymentService,
            @Qualifier("customWebhookExecutor") Executor executor,
            PaymentLockFacade paymentLockFacade) {
        this.paymentLockFacade = paymentLockFacade;
        this.paymentService = paymentService;
        this.executor = executor;
    }

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
            @AuthenticationPrincipal AuthUser authUser,
            @ModelAttribute PaymentRequest paymentRequest) {
        Long currentUserId = authUser.getId();

        return ResponseEntity.ok(Response.of(paymentLockFacade.confirmPaymentWithLock(paymentRequest, currentUserId)));
    }

    @PostMapping("/api/v1/payments/{paymentKey}/cancel")
    public ResponseEntity<Void> cancelPayment(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable String paymentKey,
            @RequestBody CancelPaymentRequest dto) {

        Long currentUserId = authUser.getId();
        paymentLockFacade.cancelPaymentWithLock(paymentKey, dto, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/payments/{paymentKey}")
    public ResponseEntity<Response<FindPaymentResponse>> findPayment(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable String paymentKey) {

        Long currentUserId = authUser.getId();
        return ResponseEntity.ok(Response.of(paymentService.findPayment(paymentKey, currentUserId)));
    }

    @PostMapping("/api/v1/payments/webhook")
    public ResponseEntity<Void> receiveWebhook(
            @RequestBody PaymentResponse dto,
            HttpServletRequest request) {

        // 웹훅 검증
        TossWebhookVerifier tossWebhookVerifier = new TossWebhookVerifier();
        if(!tossWebhookVerifier.isValidSignature(request)){
            throw new ServerException(ErrorCode.PAYMENT_WEBHOOK_DENIED);
        }

        // 웹훅 처리 (비동기)
        executor.execute(() -> paymentLockFacade.receiveWebhookWithLock(dto));

        return ResponseEntity.ok().build();
    }
}