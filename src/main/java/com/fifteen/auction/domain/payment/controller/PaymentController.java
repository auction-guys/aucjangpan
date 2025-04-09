package com.fifteen.auction.domain.payment.controller;

import com.fifteen.auction.domain.payment.dto.request.CancelPaymentRequest;
import com.fifteen.auction.domain.payment.dto.request.SavePaymentRequset;
import com.fifteen.auction.domain.payment.dto.response.ConfirmResponse;
import com.fifteen.auction.domain.payment.dto.response.FindPaymentResponse;
import com.fifteen.auction.domain.payment.service.PaymentService;
import com.fifteen.auction.global.dto.Response;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final PaymentService paymentService;

    @GetMapping("/api/v1/payment/cancelReason")
    public ResponseEntity<Void> getCancelReason(
            @RequestParam String code,
            @RequestParam String message,
            @RequestParam String orderId){

        System.out.println("결제 실패 코드: " + code);
        System.out.println("메시지: " + message);
        System.out.println("주문 ID: " + orderId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/payments/confirm")
    public ResponseEntity<Response<ConfirmResponse>> confirmPayment(
            @RequestParam String paymentType,
            @RequestParam String orderId,
            @RequestParam String amount,
            @RequestParam String paymentKey) throws Exception {
        Long currentUserId = 2L;// 테스트용

        SavePaymentRequset dto = paymentService.confirm(orderId, amount, paymentKey, currentUserId);

        ConfirmResponse confirmResponse = paymentService.savePayment(dto);

        return ResponseEntity.ok(Response.of(confirmResponse));
    }

    @PostMapping("/api/v1/payments/{paymentKey}/cancel")
    public ResponseEntity<Void> cancelPayment(
            @PathVariable String paymentKey,
            @RequestBody CancelPaymentRequest dto) throws IOException, ParseException {

        Long currentUserId = 2L;
        paymentService.cancelPaymentByUser(paymentKey, dto.getReason(), currentUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/payments/{paymentKey}")
    public ResponseEntity<Response<FindPaymentResponse>> findPayment(
            @PathVariable String paymentKey){

        Long currentUserId = 2L;
        return ResponseEntity.ok(Response.of(paymentService.findPayment(paymentKey, currentUserId)));
    }
}