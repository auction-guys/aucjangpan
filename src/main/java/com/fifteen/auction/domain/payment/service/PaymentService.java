package com.fifteen.auction.domain.payment.service;

import com.fifteen.auction.domain.order.entity.Order;
import com.fifteen.auction.domain.order.repository.OrderRepository;
import com.fifteen.auction.domain.payment.dto.response.ConfirmResponse;
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

    public ConfirmResponse confirm(String orderId, String amount, String paymentKey) throws IOException, ParseException {

        Order order = orderRepository.findById(Long.parseLong(orderId)) // 이부분 더 생각해보기 받은 order id가 아니라 보낼때 쓴 id로 받아야 할듯
                .orElseThrow(() -> new ClientException(ErrorCode.ORDER_NOT_FOUND));

        if(!order.getId().equals(Long.parseLong(orderId)) && order.getAuction().getWinPrice().equals(Long.parseLong(amount))){
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
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        // 데이터 전송 및 응답 수신
        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(obj.toString().getBytes());

        int code = connection.getResponseCode();
        boolean isSuccess = code == 200;

        InputStream responseStream = isSuccess ? connection.getInputStream() : connection.getErrorStream();

        // 결제 성공 및 실패 비즈니스 로직을 구현하세요.
        JSONParser parser = new JSONParser();
        Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8);
        JSONObject jsonObject = (JSONObject) parser.parse(reader);
        responseStream.close();

        if(!isSuccess){
            JSONObject error = (JSONObject) jsonObject.get("error");
            throw new PaymentFailException(HttpStatus.valueOf(Integer.parseInt(error.get("code").toString())), error.get("message").toString());
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
}
