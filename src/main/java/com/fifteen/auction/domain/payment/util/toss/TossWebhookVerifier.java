package com.fifteen.auction.domain.payment.util.toss;

import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ServerException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class TossWebhookVerifier {

    // 원래 보안키가 따로 있지만 현재 토스에서 제공하는 테스트키를 사용중으로 보안키 사용 불가 기존 사용하던 시크릿 키로 대체
    @Value("${toss.secret-key}")
    private String secretKey;

    // 토스페이먼츠 웹훅 서명 검증
    public boolean isValidSignature(HttpServletRequest request) {

        // 웹훅 페이로드
        String payload = getRequestBody(request);
        // 웹훅 전송 시간
        String transmissionTime = request.getHeader("tosspayments-webhook-transmission-time");
        // 웹훅 서명
        String signatureHeader = request.getHeader("tosspayments-webhook-signature");
        // HMAC 해시 생성
        String expectedSignature = generateHmacSha256(payload + ":" + transmissionTime, secretKey);

        // 서명 값 디코딩
        String[] signatureParts = signatureHeader.split(",");
        String decodedSignature1 = new String(Base64.getDecoder().decode(signatureParts[0].split(":")[1]), StandardCharsets.UTF_8);
        String decodedSignature2 = new String(Base64.getDecoder().decode(signatureParts[1].split(":")[1]), StandardCharsets.UTF_8);

        // 해시값과 서명 값 비교
        return expectedSignature.equals(decodedSignature1) || expectedSignature.equals(decodedSignature2);
    }

    // HMAC 해시 생성
    private String generateHmacSha256(String data, String secretKey) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init( new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new ServerException(ErrorCode.PAYMENT_WEBHOOK_EXCEPTION);
        }
    }

    // 페이로드 추출
    private String getRequestBody(HttpServletRequest request) {
        try {
            return new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ServerException(ErrorCode.PAYMENT_WEBHOOK_EXCEPTION);
        }
    }
}
