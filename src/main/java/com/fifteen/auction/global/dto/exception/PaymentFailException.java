package com.fifteen.auction.global.dto.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

@Getter
public class PaymentFailException extends RuntimeException {
    private final int status;
    private final String errorCode;

    public PaymentFailException(int status, String json) {
        super(getErrorMessage(json));
        this.status = status;
        this.errorCode = getErrorCode(json);
    }

    private static String getErrorMessage(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(json);
            return jsonNode.get("message").asText();
        } catch (Exception e) {
            return "결제 실패"; // 기본 메시지
        }
    }

    private static String getErrorCode(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(json);
            return jsonNode.get("code").asText();
        } catch (Exception e) {
            return "UNKNOWN_ERROR";
        }
    }


}
