package com.fifteen.auction.global.dto.exception;

import com.fifteen.auction.global.dto.error.ErrorCode;
import lombok.Getter;

@Getter
public class S3UploadException extends RuntimeException {

    private final ErrorCode errorCode;

    public S3UploadException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public S3UploadException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}