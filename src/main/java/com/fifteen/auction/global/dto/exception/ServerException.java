package com.fifteen.auction.global.dto.exception;

import com.fifteen.auction.global.dto.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
// @RequiredArgsConstructor  //오버로딩이 되는 형태라 제외
public class ServerException extends RuntimeException {
    private final ErrorCode errorCode;

    public ServerException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ServerException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}
