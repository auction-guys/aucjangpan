package com.fifteen.auction.domain.payment.service;

import com.fifteen.auction.domain.order.entity.Order;
import com.fifteen.auction.domain.order.repository.OrderRepository;
import com.fifteen.auction.domain.payment.dto.request.CancelPaymentRequest;
import com.fifteen.auction.domain.payment.dto.request.PaymentRequest;
import com.fifteen.auction.domain.payment.dto.response.PaymentResponse;
import com.fifteen.auction.domain.payment.dto.response.ConfirmResponse;
import com.fifteen.auction.domain.payment.dto.response.FindPaymentResponse;
import com.fifteen.auction.domain.payment.entity.Payment;
import com.fifteen.auction.domain.payment.repository.PaymentRepository;
import com.fifteen.auction.domain.payment.util.IdempotencyKeyGenerator;
import com.fifteen.auction.domain.payment.util.TossFeignClient;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import com.fifteen.auction.global.dto.exception.PaymentFailException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final IdempotencyKeyGenerator idempotencyKeyGenerator;
    private final TossFeignClient tossFeignClient;

    public ConfirmResponse confirm(PaymentRequest request, Long currentUserId) {

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ClientException(ErrorCode.ORDER_NOT_FOUND));

        // orderId, amount 변조 검증
        order.validatePaymentInfo(currentUserId, request.getAmount());

        // TODO: 이후 로그 생성때 로깅할 예정
        // 멱등키 생성
        String idempotencyKey = idempotencyKeyGenerator.generate();

        PaymentResponse response;
        // 승인 요청
        try {
            // 결제 정보 반
            response = tossFeignClient.confirmPayment(request, idempotencyKey);
        } catch (FeignException e) {
            // TODO: 추후 로깅
            System.out.println("요청 실패: " + e.status()); // HTTP 상태 코드
            System.out.println("응답 메시지: " + e.contentUTF8()); // 응답 body
            throw new PaymentFailException(e.status(), e.contentUTF8());
        }

        // 결제 정보 저장
        try {
            paymentRepository.save(new Payment(response, order));
            // 주문 상태 변환
            order.paid();
        } catch (Exception e) {
            // 결제 정보 저장 중 오류 - 현재는 결제 취소를 하지만 고도화 때 결제 취소 => 취소 X 로그 남김으로 변경 예정
            cancelInvalidPayment(String.valueOf(response.getPaymentKey()), new CancelPaymentRequest(e.getMessage()));
            throw e;
        }
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
}
