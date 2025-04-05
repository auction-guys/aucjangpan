package com.fifteen.auction.global.dto.exception;

import com.fifteen.auction.global.dto.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ClientException extends RuntimeException {
    private final ErrorCode errorCode;
}
