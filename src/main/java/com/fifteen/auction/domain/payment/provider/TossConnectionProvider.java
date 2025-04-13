package com.fifteen.auction.domain.payment.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TossConnectionProvider {

    private final TossAuthorizationProvider tossAuthorizationProvider;

    public HttpURLConnection createConfirmConnection() throws IOException {

        String idempotencyKey = UUID.randomUUID().toString();
        URL url = new URL("https://api.tosspayments.com/v1/payments/confirm");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestProperty("Authorization", tossAuthorizationProvider.getBasicAuth());
        connection.setRequestProperty("Idempotency-Key", idempotencyKey);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        return connection;
    }

    public HttpURLConnection createCanclePaymentConnection(String paymentKey) throws IOException {

        String idempotencyKey = UUID.randomUUID().toString();
        URL url = new URL("https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestProperty("Authorization", tossAuthorizationProvider.getBasicAuth());
        connection.setRequestProperty("Idempotency-Key", idempotencyKey);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        return connection;
    }
}
