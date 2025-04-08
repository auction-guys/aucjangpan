package com.fifteen.auction.domain.payment.service;

import com.fifteen.auction.domain.order.entity.Order;
import com.fifteen.auction.domain.order.repository.OrderRepository;
import com.fifteen.auction.domain.payment.dto.request.SavePaymentRequset;
import com.fifteen.auction.domain.payment.dto.response.ConfirmResponse;
import com.fifteen.auction.domain.payment.dto.response.FindPaymentResponse;
import com.fifteen.auction.domain.payment.entity.Payment;
import com.fifteen.auction.domain.payment.enums.PaymentStatus;
import com.fifteen.auction.domain.payment.repository.PaymentRepository;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import com.fifteen.auction.global.dto.exception.PaymentFailException;
import com.fifteen.auction.global.dto.exception.ServerException;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    public SavePaymentRequset confirm(String orderId, String amount, String paymentKey, Long loginedId) throws IOException, ParseException {

        Order order = orderRepository.findById(Long.parseLong(orderId))
                .orElseThrow(() -> new ClientException(ErrorCode.ORDER_NOT_FOUNDED));

        // orderId, amount 변조 검증
        if(!order.getUser().getId().equals(loginedId) && order.getAuction().getWinPrice().equals(Long.parseLong(amount))){
            throw new ServerException(ErrorCode.ORDER_NOT_MATCHED);
        }

        // Toss 승인 요청 API JSON 생성
        JSONObject obj = new JSONObject();
        obj.put("orderId", orderId);
        obj.put("amount", amount);
        obj.put("paymentKey", paymentKey);

        // Toss 인증용 헤더 설정
        // 토스페이먼츠 API는 시크릿 키를 사용자 ID로 사용하고, 비밀번호는 사용하지 않습니다.
        // 비밀번호가 없다는 것을 알리기 위해 시크릿 키 뒤에 콜론을 추가합니다.
        String widgetSecretKey = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6";
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encodedBytes = encoder.encode((widgetSecretKey + ":").getBytes(StandardCharsets.UTF_8));
        String authorizations = "Basic " + new String(encodedBytes);

        // HTTP POST 요청
        // 결제를 승인하면 결제수단에서 금액이 차감돼요.
        URL url = new URL("https://api.tosspayments.com/v1/payments/confirm");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", authorizations);
        connection.setRequestProperty("Idempotency-Key", order.getIdempotencyKey()); // 멱등키
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        // 데이터 전송 및 응답 수신
        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(obj.toString().getBytes());

        int code = connection.getResponseCode();
        boolean isSuccess = code == 200;

        InputStream responseStream = isSuccess ? connection.getInputStream() : connection.getErrorStream();

        JSONParser parser = new JSONParser();
        Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8);
        JSONObject jsonObject = (JSONObject) parser.parse(reader);
        responseStream.close();

        // 실패
        if(!isSuccess){
            JSONObject error = (JSONObject) jsonObject.get("error");
            throw new PaymentFailException(HttpStatus.valueOf(Integer.parseInt(error.get("code").toString())), error.get("message").toString());
        }

        // 성공
        return SavePaymentRequset.builder()
                .jsonObject(jsonObject)
                .order(order)
                .build();
    }

    // 결제 정보 저장
    @Transactional
    public ConfirmResponse savePayment(SavePaymentRequset dto) throws IOException, ParseException {
        JSONObject jsonObject = dto.getJsonObject();
        Order order = dto.getOrder();

        // 결제 정보 검증
        try{
            if(!order.getId().equals(jsonObject.get("orderId")) && order.getAuction().getWinPrice().equals(jsonObject.get("amount"))){

                throw new ClientException(ErrorCode.PAYMENT_INFO_EXCEPTION);
            }
        }catch (Exception e){
            // 결제 정보 오류 시 결제 취소
            cancelInvalidPayment(String.valueOf(jsonObject.get("paymentKey")),e.getMessage(),order.getId());
            throw e;
        }

        // 결제 정보 저장
        try {
            // 들어갈 데이터 타입 변환    이후 깔끔하게 할 방법 찾기
            String rawRequest = String.valueOf(jsonObject.get("requestedAt"));
            String requestedAt = rawRequest.substring(0, 19);
            String rawApprovedAt = String.valueOf(jsonObject.get("approvedAt"));
            String approvedAt = rawApprovedAt.substring(0, 19);
            JSONObject card = (JSONObject) jsonObject.get("card");
            String cardNumber = card.get("number").toString();

            paymentRepository.save(Payment.builder()
                            .mid(String.valueOf(jsonObject.get("mid")))
                            .paymentKey(String.valueOf(jsonObject.get("paymentKey")))
                            .paymentMethod(String.valueOf(jsonObject.get("paymentMethod")))
                            .cardNumber(cardNumber)
                            .amount(Long.parseLong(String.valueOf(jsonObject.get("amount"))))
                            .status(PaymentStatus.valueOf(String.valueOf(jsonObject.get("status"))))
                            .requestedAt(LocalDateTime.parse(requestedAt))
                            .approvedAt(LocalDateTime.parse(approvedAt))
                            .order(order)
                            .build());
            // 주문 상태 변환
            dto.getOrder().paid();
        } catch (Exception e) {
            // 결제 정보 저장 중 오류 - 현재는 결제 취소를 하지만 고도화 때 결제 취소 => 취소 X 로그 남김으로 변경 예정
            cancelInvalidPayment(String.valueOf(jsonObject.get("paymentKey")),e.getMessage(),order.getId());
            throw e;
        }

        // 주문 성공, 데이터 저장 성공
        return new ConfirmResponse(
            jsonObject.get("mid").toString(),
            jsonObject.get("paymentKey").toString(),
            jsonObject.get("paymentMethod").toString(),
            Long.parseLong(jsonObject.get("amount").toString()),
            LocalDateTime.parse(jsonObject.get("requestedAt").toString()),
            LocalDateTime.parse(jsonObject.get("approvedAt").toString())
        );
    }


    // 결제 취소 검증 - 구매자 결제 취소
    @Transactional
    public void cancelPaymentByUser(String paymentKey, String cancelReason, Long loginedId) throws IOException, ParseException {
        Payment payment = paymentRepository.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new ClientException(ErrorCode.PAYMENT_NOT_FOUNDED));
        Order order = payment.getOrder();
        // 권한 검증
        if(!order.getUser().getId().equals(loginedId)){
            throw new ClientException(ErrorCode.PAYMENT_ACCESS_DENIED);
        }
        cancelPayment(paymentKey, cancelReason, order.getId());
        payment.cancel(); // 이게 환불인가 환불을 구현하는가
    }
    // 결제 취소 검증 - 결제 중 오류로 결제 취소
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cancelInvalidPayment(String paymentKey, String cancelReason, Long orderId) throws IOException, ParseException {
        cancelPayment(paymentKey, cancelReason, orderId);
    }

    // 결제 취소 공통 로직
    public void cancelPayment(String paymentKey, String cancelReason, Long orderId) throws IOException, ParseException {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ClientException(ErrorCode.ORDER_NOT_FOUNDED));

        // Toss 인증용 헤더 설정
        // 토스페이먼츠 API는 시크릿 키를 사용자 ID로 사용하고, 비밀번호는 사용하지 않습니다.
        // 비밀번호가 없다는 것을 알리기 위해 시크릿 키 뒤에 콜론을 추가합니다.
        String widgetSecretKey = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6";
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encodedBytes = encoder.encode((widgetSecretKey + ":").getBytes(StandardCharsets.UTF_8));
        String authorizations = "Basic " + new String(encodedBytes);

        // HTTP POST 요청
        // 결제 취소 요청
        URL url = new URL("https://api.tosspayments.com/v1/payments/"+paymentKey+"/cancel");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", authorizations);
        connection.setRequestProperty("Idempotency-Key", order.getIdempotencyKey()); // 멱등키
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        // 전송
        String obj = "{\"cancelReason\":\""+cancelReason+"\"}";
        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(obj.getBytes(StandardCharsets.UTF_8));
        }

        // 응답
        int code = connection.getResponseCode();
        boolean isSuccess = code == 200;
        InputStream responseStream = isSuccess ? connection.getInputStream() : connection.getErrorStream(); // 응답 본문 스트림
        JSONParser parser = new JSONParser();
        Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8); // 스트림 → 리더
        JSONObject jsonObject = (JSONObject) parser.parse(reader); // JSON 파싱
        responseStream.close();

        // 실패
        if(!isSuccess){
            JSONObject error = (JSONObject) jsonObject.get("error");
            throw new PaymentFailException(HttpStatus.valueOf(Integer.parseInt(error.get("code").toString())), error.get("message").toString());
        }
    }

    @Transactional(readOnly = true)
    public FindPaymentResponse findPayment(String paymentKey, Long loginedId) {
        Payment payment = paymentRepository.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new ClientException(ErrorCode.PAYMENT_NOT_FOUNDED));
        Order order = payment.getOrder();
        // 권한 검증
        if(!order.getUser().getId().equals(loginedId)){
            throw new ClientException(ErrorCode.PAYMENT_ACCESS_DENIED);
        }

        return FindPaymentResponse.builder()
                .paymentKey(payment.getPaymentKey())
                .orderId(String.valueOf(order.getId()))
                .orderName(order.getAuction().getProduct().getName())
                .status(payment.getStatus().toString())
                .requestAt(payment.getRequestedAt().toString())
                .approvedAt(payment.getApprovedAt().toString())
                .paymentMethod(payment.getPaymentMethod())
                .cardNumber(payment.getCardNumber())
                .amount(order.getAuction().getWinPrice().toString())
                .build();
    }
}
