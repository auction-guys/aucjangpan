package com.fifteen.auction.global.dto.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PaymentFailException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    public PaymentFailException(HttpStatus status, String errorCode, String customMessage) {
        super(customMessage);
        this.status = status;
        this.errorCode = errorCode;
    }
}
