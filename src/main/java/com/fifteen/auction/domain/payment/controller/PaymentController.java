package com.fifteen.auction.domain.payment.controller;

import com.fifteen.auction.domain.payment.dto.response.ConfirmResponse;
import com.fifteen.auction.domain.payment.service.PaymentService;
import com.fifteen.auction.global.dto.Response;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final PaymentService paymentService;

    @PostMapping("api/v1/payments/confirm")
    public ResponseEntity<Response<ConfirmResponse>> confirmPayment(
            @RequestParam String orderId,
            @RequestParam String amount,
            @RequestParam String paymentKey) throws Exception {

        return ResponseEntity.ok(Response.of(paymentService.confirm(orderId, amount, paymentKey)));
    }
}