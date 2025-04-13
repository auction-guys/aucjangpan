package com.fifteen.auction.domain.payment.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ServerException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class HttpRequestWriter {

    private final ObjectMapper objectMapper;

    public void writeJson(OutputStream outputStream, Object requestDto) {
        try {
            String json = objectMapper.writeValueAsString(requestDto);
            System.out.println("json"+json);
            outputStream.write(json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new ServerException(ErrorCode.PAYMENT_FAILED);
        }
    }
}
