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

        pe.printStackTrace();

        Map<String, Object> body = new HashMap<>();
        body.put("error", pe.getErrorCode());
        body.put("status", pe.getStatus());
        body.put("message", pe.getMessage());

        return ResponseEntity.status(pe.getStatus()).body(body);
    }

    @ExceptionHandler(ClientException.class)
    public ResponseEntity<ErrorResponse> handleClientException(ClientException ce) {
        ce.printStackTrace();
        return ResponseEntity.status(ce.getErrorCode().getStatus())
                .body(ErrorResponse.ofErrorCode(ce.getErrorCode()));
    }

    @ExceptionHandler(ServerException.class)
    public ResponseEntity<ErrorResponse> handleServerException(ServerException se) {
        se.printStackTrace();
        return ResponseEntity.internalServerError()
                .body(ErrorResponse.ofErrorCode(se.getErrorCode()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        e.printStackTrace();
        log.debug("exceptionHandler", e);

        return ResponseEntity.internalServerError()
                .body(ErrorResponse.ofErrorCode(ErrorCode.EXCEPTION));
    }
}
