package com.fifteen.auction.global.dto.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PaymentFailException extends RuntimeException {
    private final HttpStatus status;

    public PaymentFailException(HttpStatus status, String customMessage) {
        super(customMessage);
        this.status = status;
    }
}
