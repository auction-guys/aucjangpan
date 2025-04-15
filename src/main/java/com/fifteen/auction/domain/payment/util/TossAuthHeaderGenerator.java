package com.fifteen.auction.domain.payment.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class TossAuthHeaderGenerator {

    @Value("${toss.secret-key}")
    private String secretKey;

    // 인증용 헤더 생성
    public String getBasicAuth() {
        String base = secretKey + ":";
        return "Basic " + Base64.getEncoder().encodeToString(base.getBytes(StandardCharsets.UTF_8));
    }
}
