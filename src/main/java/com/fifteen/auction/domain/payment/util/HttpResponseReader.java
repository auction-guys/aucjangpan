package com.fifteen.auction.domain.payment.util;

import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.PaymentFailException;
import com.fifteen.auction.global.dto.exception.ServerException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

@Component
public class HttpResponseReader {

    public JSONObject parseJson(HttpURLConnection connection) {

        try {
            int code = connection.getResponseCode();
            boolean isSuccess = code == 200;

            try (
                InputStream responseStream = isSuccess ? connection.getInputStream() : connection.getErrorStream();
                Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8)
            ) {
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(reader);

                if (!isSuccess) {
                    throw new PaymentFailException(
                            HttpStatus.valueOf(code),
                            jsonObject.get("code").toString(),
                            jsonObject.get("message").toString()
                    );
                }

                return jsonObject;
            }
        } catch (IOException | ParseException e) {

            throw new ServerException(ErrorCode.PAYMENT_FAILED);
        }
    }
}
