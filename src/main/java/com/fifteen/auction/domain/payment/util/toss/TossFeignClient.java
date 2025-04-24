package com.fifteen.auction.domain.payment.util.toss;

import com.fifteen.auction.domain.payment.dto.request.CancelPaymentRequest;
import com.fifteen.auction.domain.payment.dto.request.PaymentRequest;
import com.fifteen.auction.domain.payment.dto.response.PaymentResponse;
import com.fifteen.auction.global.config.TossFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "payment", url = "https://api.tosspayments.com/v1", configuration = TossFeignConfig.class)
public interface TossFeignClient {

    @PostMapping("/payments/confirm")
    PaymentResponse confirmPayment(
            @RequestBody PaymentRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey);

    @PostMapping("/payments/{paymentKey}/cancel")
    PaymentResponse cancelPayment(
            @PathVariable("paymentKey") String paymentKey,
            @RequestBody CancelPaymentRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey);
}
