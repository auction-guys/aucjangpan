package com.fifteen.auction.domain.payment.service;

import com.fifteen.auction.domain.order.entity.Order;
import com.fifteen.auction.domain.order.repository.OrderRepository;
import com.fifteen.auction.domain.payment.dto.request.CancelPaymentRequest;
import com.fifteen.auction.domain.payment.dto.request.PaymentRequest;
import com.fifteen.auction.domain.payment.dto.request.PaymentResponse;
import com.fifteen.auction.domain.payment.dto.response.ConfirmResponse;
import com.fifteen.auction.domain.payment.dto.response.FindPaymentResponse;
import com.fifteen.auction.domain.payment.entity.Payment;
import com.fifteen.auction.domain.payment.provider.TossConnectionProvider;
import com.fifteen.auction.domain.payment.repository.PaymentRepository;
import com.fifteen.auction.domain.payment.util.HttpRequestWriter;
import com.fifteen.auction.domain.payment.util.HttpResponseReader;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.HttpURLConnection;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final TossConnectionProvider tossConnectionProvider;
    private final HttpRequestWriter requestWriter;
    private final HttpResponseReader responseReader;

    public PaymentResponse confirm(PaymentRequest request, Long currentUserId) throws IOException {

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ClientException(ErrorCode.ORDER_NOT_FOUND));

        // orderId, amount 변조 검증
        order.validatePaymentInfo(currentUserId, request.getAmount());

        // HTTP POST 요청 작성
        HttpURLConnection connection = tossConnectionProvider.createConfirmConnection();

        // 데이터 전송
        requestWriter.writeJson(connection.getOutputStream(), request);

        // 응답 수신
        JSONObject jsonObject = responseReader.parseJson(connection);

        // 결제 정보 반환
        return new PaymentResponse(jsonObject, order);
    }

    // 결제 정보 저장
    @Transactional
    public ConfirmResponse savePayment(PaymentResponse dto) throws IOException, ParseException {
        // 결제 정보 검증
        try {
            dto.getOrder().validatePaymentInfo(Long.parseLong(dto.getOrder().getUser().getId().toString()), dto.getAmount());
        } catch (Exception e) {
            // 결제 정보 오류 시 결제 취소
            cancelInvalidPayment(String.valueOf(dto.getPaymentKey()), new CancelPaymentRequest(e.getMessage()));
            throw e;
        }
        // 결제 정보 저장
        try {
            paymentRepository.save(new Payment(dto.getJsonObject(), dto.getOrder()));
            // 주문 상태 변환
            dto.getOrder().paid();
        } catch (Exception e) {
            // 결제 정보 저장 중 오류 - 현재는 결제 취소를 하지만 고도화 때 결제 취소 => 취소 X 로그 남김으로 변경 예정
            cancelInvalidPayment(String.valueOf(dto.getPaymentKey()), new CancelPaymentRequest(e.getMessage()));
            throw e;
        }
        // 주문 성공, 데이터 저장 성공
        return new ConfirmResponse(dto.getJsonObject());
    }


    // 결제 취소 검증 - 구매자 결제 취소
    @Transactional
    public void cancelPaymentByUser(String paymentKey, CancelPaymentRequest dto, Long currentUserId) throws IOException, ParseException {
        Payment payment = paymentRepository.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new ClientException(ErrorCode.PAYMENT_NOT_FOUND));
        // 권한 검증
        payment.validateOwner(currentUserId);

        cancelPayment(paymentKey, dto);

        // TODO: 현재는 그냥 취소인데 취소가 주문 취소 까지 가는지
        payment.cancel();
    }

    // 결제 취소 검증 - 결제 중 오류로 결제 취소
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cancelInvalidPayment(String paymentKey, CancelPaymentRequest dto) throws IOException, ParseException {
        cancelPayment(paymentKey, dto);
    }

    // 결제 취소 공통 로직
    public void cancelPayment(String paymentKey, CancelPaymentRequest dto) throws IOException {

        // 결제 취소 요청 작성
        HttpURLConnection connection = tossConnectionProvider.createCanclePaymentConnection(paymentKey);

        // 전송
        requestWriter.writeJson(connection.getOutputStream(), dto);

        // 응답
        responseReader.parseJson(connection);
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
