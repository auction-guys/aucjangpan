package com.fifteen.auction.domain.payment.controller;

import com.fifteen.auction.domain.payment.dto.request.CancelPaymentRequest;
import com.fifteen.auction.domain.payment.dto.request.SavePaymentRequset;
import com.fifteen.auction.domain.payment.dto.response.ConfirmResponse;
import com.fifteen.auction.domain.payment.dto.response.FindPaymentResponse;
import com.fifteen.auction.domain.payment.service.PaymentService;
import com.fifteen.auction.global.dto.Response;
import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final PaymentService paymentService;

    @PostMapping("/api/v1/payments/confirm")
    public ResponseEntity<Response<ConfirmResponse>> confirmPayment(
            @RequestParam String orderId,
            @RequestParam String amount,
            @RequestParam String paymentKey,
            Long loginedId) throws Exception {

        SavePaymentRequset dto = paymentService.confirm(orderId, amount, paymentKey, loginedId);

        ConfirmResponse confirmResponse = paymentService.savePayment(dto);

        return ResponseEntity.ok(Response.of(confirmResponse));
    }

    @PostMapping("/api/v1/payments/{paymentKey}/cancel")
    public ResponseEntity<Void> cancelPayment(
            @PathVariable String paymentKey,
            @RequestBody CancelPaymentRequest dto,
            Long loginedId) throws IOException, ParseException {

        paymentService.cancelPaymentByUser(paymentKey, dto.getResson(), loginedId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/api/v1/payments/{paymentKey}")
    public ResponseEntity<Response<FindPaymentResponse>> findPayment(
            @PathVariable String paymentKey,
            Long loginedId){

        return ResponseEntity.ok(Response.of(paymentService.findPayment(paymentKey, loginedId)));
    }
}