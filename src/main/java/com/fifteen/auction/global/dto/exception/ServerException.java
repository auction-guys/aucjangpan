package com.fifteen.auction.global.dto.exception;

import com.fifteen.auction.global.dto.error.ErrorCode;
import lombok.Getter;

@Getter
public class ServerException extends RuntimeException {
    private final ErrorCode errorCode;

    public ServerException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
