package com.fifteen.auction.global.dto.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@Getter
@JsonInclude(Include.NON_NULL)
public class ErrorResponse<T> {
    private final String name;
    private final String code;
    private final String message;
    private T data;

    private ErrorResponse(String name, String code, String message) {
        this.name = name;
        this.code = code;
        this.message = message;
    }

    private ErrorResponse(String name, String code, String message, T data) {
        this(name, code, message);
        this.data = data;
    }

    public static ErrorResponse ofErrorCode(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.name(), errorCode.getCode(), errorCode.getMessage());
    }

    public static <T> ErrorResponse<T> withData(ErrorCode errorCode, T data) {
        return new ErrorResponse<>(errorCode.name(), errorCode.getCode(), errorCode.getMessage(), data);
    }
}
