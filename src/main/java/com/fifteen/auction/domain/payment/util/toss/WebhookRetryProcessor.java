package com.fifteen.auction.domain.payment.util.toss;

import com.fifteen.auction.domain.payment.entity.Payment;
import com.fifteen.auction.domain.payment.repository.PaymentRepository;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookRetryProcessor {

    private final RedisTemplate<String, String> redisTemplate;
    private final PaymentRepository paymentRepository;

    private static final String RETRY_QUEUE = "webhook:retry";
    private static final int MAX_RETRIES = 3;
    private static final String RETRY_COUNT_KEY_PREFIX = "webhook:retry:count:";

    //@Scheduled(fixedDelay = 10000) // 10초마다 실행_이후 서비스가 어느정도 사이즈냐에 따라 조정
    public void processRetryQueue() {
        // Redis 리스트에서 paymentKey 꺼내기
        String paymentKey = redisTemplate.opsForList().rightPop(RETRY_QUEUE);
        if (paymentKey == null) {
            return; // 큐가 비어 있으면 종료
        }

        log.info("재시도 큐 처리 시작: paymentKey: {}", paymentKey);

        // 재시도 횟수 확인
        String retryCountKey = RETRY_COUNT_KEY_PREFIX + paymentKey;
        Long retryCount = redisTemplate.opsForValue().increment(retryCountKey, 1);
        if (retryCount == null || retryCount > MAX_RETRIES) {
            log.error("최대 재시도 횟수 초과: paymentKey: {}", paymentKey);
            redisTemplate.delete(retryCountKey); // 재시도 카운트 삭제
            throw new ServerException(ErrorCode.PAYMENT_WEBHOOK_RETRY_EXCEPTION);
        }

        // 결제 정보 확인
        Optional<Payment> payment = paymentRepository.findByPaymentKey(paymentKey);
        if (payment.isPresent()) {
            Payment donePayment = payment.get();
            String orderId = donePayment.getOrder().getId();
            log.info("결제 정보 확인: paymentKey: {}, orderId: {}", paymentKey, orderId);

            // 재시도 카운트 삭제
            redisTemplate.delete(retryCountKey);
        } else {
            log.warn("결제 정보 없음: paymentKey: {}", paymentKey);
            redisTemplate.opsForList().leftPush(RETRY_QUEUE, paymentKey);
        }
    }
}
