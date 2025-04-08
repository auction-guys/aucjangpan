package com.fifteen.auction.domain.payment.service;

import com.fifteen.auction.domain.order.entity.Order;
import com.fifteen.auction.domain.order.repository.OrderRepository;
import com.fifteen.auction.domain.order.service.OrderService;
import com.fifteen.auction.domain.payment.dto.request.SavePaymentRequset;
import com.fifteen.auction.domain.payment.dto.response.ConfirmResponse;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final OrderService orderService;

    public SavePaymentRequset confirm(String orderId, String amount, String paymentKey, Long loginedId) throws IOException, ParseException {

        Order order = orderRepository.findById(Long.parseLong(orderId))
                .orElseThrow(() -> new ClientException(ErrorCode.ORDER_NOT_FOUNDED));

        // orderId, amount 변조 검증
        if(!order.getUser().getId().equals(loginedId) && order.getAuction().getWinPrice().equals(Long.parseLong(amount))){
            throw new ServerException(ErrorCode.ORDER_NOT_MACHED);
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

        return SavePaymentRequset.builder()
                .jsonObject(jsonObject)
                .order(order)
                .build();
    }

    @Transactional
    public ConfirmResponse savePayment(SavePaymentRequset dto, String orderId, String amount) throws IOException, ParseException {
        JSONObject jsonObject = dto.getJsonObject();

        try {
            if(!String.valueOf(jsonObject.get("orderId")).equals(orderId) && String.valueOf(jsonObject.get("amount")).equals(amount)){
                throw new ClientException(ErrorCode.ORDER_NOT_MACHED);
            }

            String rawRequest = String.valueOf(jsonObject.get("requestedAt"));
            String requestedAt = rawRequest.substring(0, 19);
            String rawApprovedAt = String.valueOf(jsonObject.get("approvedAt"));
            String approvedAt = rawApprovedAt.substring(0, 19);

            paymentRepository.save(Payment.builder()
                            .mid(String.valueOf(jsonObject.get("mid")))
                            .paymentKey(String.valueOf(jsonObject.get("paymentKey")))
                            .paymentMethod(String.valueOf(jsonObject.get("paymentMethod")))
                            .amount(Long.parseLong(String.valueOf(jsonObject.get("amount"))))
                            .status(PaymentStatus.valueOf(String.valueOf(jsonObject.get("status"))))
                            .requestedAt(LocalDateTime.parse(requestedAt))
                            .approvedAt(LocalDateTime.parse(approvedAt))
                            .order(orderRepository.findById(Long.parseLong(orderId)).get())
                            .build());
            dto.getOrder().paid();

        } catch (Exception e) {
            String paymentKey = String.valueOf(jsonObject.get("paymentKey"));
            cancelPayment(paymentKey, Long.parseLong(orderId), e.getMessage());
            throw e;
        }

        return new ConfirmResponse(
            jsonObject.get("mid").toString(),
            jsonObject.get("paymentKey").toString(),
            jsonObject.get("paymentMethod").toString(),
            Long.parseLong(jsonObject.get("amount").toString()),
            LocalDateTime.parse(jsonObject.get("requestedAt").toString()),
            LocalDateTime.parse(jsonObject.get("approvedAt").toString())
        );
    }

    // 결제 취소
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cancelPayment(String paymentKey, Long orderId, String cancelReason) throws IOException, ParseException {
        // Toss 인증용 헤더 설정
        // 토스페이먼츠 API는 시크릿 키를 사용자 ID로 사용하고, 비밀번호는 사용하지 않습니다.
        // 비밀번호가 없다는 것을 알리기 위해 시크릿 키 뒤에 콜론을 추가합니다.
        String widgetSecretKey = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6";
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encodedBytes = encoder.encode((widgetSecretKey + ":").getBytes(StandardCharsets.UTF_8));
        String authorizations = "Basic " + new String(encodedBytes);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ClientException(ErrorCode.ORDER_NOT_FOUNDED));

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

        Optional<Payment> payment = paymentRepository.findByPaymentKey(paymentKey);
        payment.ifPresent(Payment::cancel);
    }
}
