package com.fifteen.auction.global.controller;

import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.error.ErrorResponse;
import com.fifteen.auction.global.dto.exception.ClientException;
import com.fifteen.auction.global.dto.exception.PaymentFailException;
import com.fifteen.auction.global.dto.exception.ServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentFailException.class)
    public ResponseEntity<Map<String, Object>> handlePaymentFailException(PaymentFailException pe) {

        Map<String, Object> body = new HashMap<>();
        body.put("error", "PaymentFailException");
        body.put("status", "PAYMENT_2");
        body.put("message", pe.getMessage());

        return ResponseEntity.status(pe.getStatus()).body(body);
    }


    @ExceptionHandler(ClientException.class)
    public ResponseEntity<ErrorResponse> handleClientException(ClientException ce) {
        return ResponseEntity.status(ce.getErrorCode().getStatus())
                .body(ErrorResponse.ofErrorCode(ce.getErrorCode()));
    }

    @ExceptionHandler(ServerException.class)
    public ResponseEntity<ErrorResponse> handleServerException(ServerException se) {
        return ResponseEntity.internalServerError()
                .body(ErrorResponse.ofErrorCode(se.getErrorCode()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {

        log.debug("exceptionHandler", e);

        return ResponseEntity.internalServerError()
                .body(ErrorResponse.ofErrorCode(ErrorCode.EXCEPTION));
    }
}
