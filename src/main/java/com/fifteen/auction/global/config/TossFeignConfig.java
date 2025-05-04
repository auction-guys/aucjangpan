package com.fifteen.auction.global.config;

import com.fifteen.auction.domain.payment.util.toss.TossAuthHeaderGenerator;
import feign.RequestInterceptor;
import feign.codec.Encoder;
import feign.jackson.JacksonEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TossFeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor(TossAuthHeaderGenerator provider) {
        return requestTemplate -> {
            requestTemplate.header("Authorization", provider.getBasicAuth());
            requestTemplate.header("Content-Type", "application/json");
        };
    }
    @Bean
    public Encoder feignEncoder() {
        return new JacksonEncoder();
    }
}
