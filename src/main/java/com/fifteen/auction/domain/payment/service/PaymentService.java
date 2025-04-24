package com.fifteen.auction.domain.payment.service;

import com.fifteen.auction.domain.order.entity.Order;
import com.fifteen.auction.domain.order.repository.OrderRepository;
import com.fifteen.auction.domain.payment.dto.request.CancelPaymentRequest;
import com.fifteen.auction.domain.payment.dto.request.PaymentRequest;
import com.fifteen.auction.domain.payment.dto.response.PaymentResponse;
import com.fifteen.auction.domain.payment.dto.response.ConfirmResponse;
import com.fifteen.auction.domain.payment.dto.response.FindPaymentResponse;
import com.fifteen.auction.domain.payment.entity.Payment;
import com.fifteen.auction.domain.payment.enums.PaymentStatus;
import com.fifteen.auction.domain.payment.repository.PaymentRepository;
import com.fifteen.auction.domain.payment.util.IdempotencyKeyGenerator;
import com.fifteen.auction.domain.payment.util.toss.TossFeignClient;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import com.fifteen.auction.global.dto.exception.PaymentFailException;
import com.fifteen.auction.global.dto.exception.ServerException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final IdempotencyKeyGenerator idempotencyKeyGenerator;
    private final TossFeignClient tossFeignClient;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public ConfirmResponse confirm(PaymentRequest request, Long currentUserId) {

        // 중복 결제 검증
        paymentRepository.findByPaymentKey(request.getPaymentKey())
                .ifPresent(payment -> {
                    log.info("이미 처리된 결제입니다: paymentKey: {}, orderId: {}", request.getPaymentKey(), request.getOrderId());
                    throw new ClientException(ErrorCode.PAYMENT_ALREADY_PROCESSED); // 예외를 던져서 메서드 종료
                });

        // orderId, amount 변조 검증
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ClientException(ErrorCode.ORDER_NOT_FOUND));
        order.validatePaymentInfo(currentUserId, Long.parseLong(request.getAmount()));

        // TODO: 로그 컨벤션 얘기해보기
        // 멱등키 생성
        String idempotencyKey = idempotencyKeyGenerator.generate();
        log.info("멱등키 생성! {}", idempotencyKey);

        PaymentResponse response;
        // 승인 요청
        try {
            // 결제 정보 반
            response = tossFeignClient.confirmPayment(request, idempotencyKey);
        } catch (FeignException e) {
            log.error("결제 실패", e);
            log.error("응답내용: {}", e.contentUTF8());
            throw new PaymentFailException(e.status(), e.contentUTF8());
        }

        // 결제 정보 저장
        try {
            paymentRepository.save(new Payment(response, order));
        } catch (DataIntegrityViolationException e) {
            log.warn("결제 정보 중복 저장  paymentKey: {}, orderId: {}", response.getPaymentKey(), response.getOrderId());
            Payment payment = paymentRepository.findByPaymentKey(response.getPaymentKey())
                    .orElseThrow(() -> new ClientException(ErrorCode.PAYMENT_NOT_FOUND));
            // 웹훅으로 먼저 들어온 정보와 일치한지 검증
            payment.check(response);
        } catch (Exception e) {
            log.error("결제 정보 저장 실패", e);
            cancelInvalidPayment(String.valueOf(response.getPaymentKey()), new CancelPaymentRequest(e.getMessage()));
            throw e;
        }

        // 주문 상태 변환
        order.paid();

        // 주문 성공, 데이터 저장 성공
        return new ConfirmResponse(response);
    }


    // 결제 취소 검증 - 구매자 결제 취소
    @Transactional
    public void cancelPaymentByUser(String paymentKey, CancelPaymentRequest dto, Long currentUserId) {
        Payment payment = paymentRepository.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new ClientException(ErrorCode.PAYMENT_NOT_FOUND));
        // 권한 검증
        payment.validateOwner(currentUserId);

        cancelPayment(paymentKey, dto);

        // TODO: 현재는 그냥 취소인데 취소가 주문 취소 까지 가는지
        payment.cancel();
    }

    // 결제 취소 검증 - 결제 중 오류로 결제 취소
    public void cancelInvalidPayment(String paymentKey, CancelPaymentRequest dto) {
        cancelPayment(paymentKey, dto);
    }

    // 결제 취소 공통 로직
    public void cancelPayment(String paymentKey, CancelPaymentRequest dto) {

        String idempotencyKey = idempotencyKeyGenerator.generate();

        tossFeignClient.cancelPayment(paymentKey, dto, idempotencyKey);
    }

    @Transactional(readOnly = true)
    public FindPaymentResponse findPayment(String paymentKey, Long currentUserId) {
        Payment payment = paymentRepository.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new ClientException(ErrorCode.PAYMENT_NOT_FOUND));
        // 권한 검증
        payment.validateOwner(currentUserId);

        return FindPaymentResponse.from(payment);
    }

    @Transactional
    public void receiveWebhook(PaymentResponse dto) {

        String paymentKey = dto.getPaymentKey();
        String orderId = dto.getOrderId();
        PaymentStatus status = dto.getStatus();

        switch (status) {
            case DONE -> {
                paymentRepository.findByPaymentKey(paymentKey)
                        .ifPresentOrElse(
                                payment -> log.info("결제 성공 알림: paymentKey: {}, orderId: {}", paymentKey, orderId),
                                () -> {
                                    log.warn("결제 성공 알림 수신했지만 payment 정보 없음: paymentKey: {}", paymentKey);
                                    redisTemplate.opsForList().leftPush("webhook:retry:done", paymentKey);
                                    log.info("재시도 큐에 추가: queue=webhook:retry:done, paymentKey: {}", paymentKey);
                                }
                        );
            }
            case EXPIRED -> {
                Optional<Payment> payment = paymentRepository.findByPaymentKey(paymentKey);
                if (payment.isEmpty()) {
                    Order order = orderRepository.findById(orderId)
                            .orElseThrow(() -> new ClientException(ErrorCode.ORDER_NOT_FOUND));
                    // 웹훅의 정보를 받아서 결제정보 생성
                    paymentRepository.save(new Payment(dto, order));
                    log.info("결제 만료 알림: paymentKey: {}, orderId: {}", paymentKey, orderId);
                } else {
                    log.warn("웹훅 정보와 심각한 불일치 발생: paymentKey: {}, orderId: {}", paymentKey, orderId);
                    throw new ServerException(ErrorCode.PAYMENT_WEBHOOK_UNMATCHED);
                }
            }
            case ABORTED -> {
                Optional<Payment> payment = paymentRepository.findByPaymentKey(paymentKey);
                if (payment.isEmpty()) {
                    Order order = orderRepository.findById(orderId)
                            .orElseThrow(() -> new ClientException(ErrorCode.ORDER_NOT_FOUND));
                    paymentRepository.save(new Payment(dto, order));
                    log.info("결제 승인 실패 알림: paymentKey: {}, orderId: {}", paymentKey, orderId);
                } else {
                    log.warn("웹훅 정보와 심각한 불일치 발생: paymentKey: {}, orderId: {}", paymentKey, orderId);
                    throw new ServerException(ErrorCode.PAYMENT_WEBHOOK_UNMATCHED);
                }
            }
            case CANCELED -> {
                Optional<Payment> payment = paymentRepository.findByPaymentKey(paymentKey);

                if (payment.isEmpty()) {
                    Order order = orderRepository.findById(orderId)
                            .orElseThrow(() -> new ClientException(ErrorCode.ORDER_NOT_FOUND));
                    Payment canceled = new Payment(dto, order);
                    paymentRepository.save(canceled);
                    log.info("결제 취소 알림: paymentKey: {}, orderId: {}", paymentKey, orderId);
                } else {
                    if (!payment.get().getStatus().equals(PaymentStatus.CANCELED)) {
                        payment.get().cancel();
                    }
                    log.info("결제 취소 처리 완료: paymentKey: {}, orderId: {}", paymentKey, orderId);
                }
            }
        }
    }
}
