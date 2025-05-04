package com.fifteen.auction.global.config;

import feign.RequestInterceptor;
import feign.codec.Encoder;
import feign.jackson.JacksonEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NaverFeignConfig {

    @Value("${naver.api.id}")
    private String clientId;

    @Value("${naver.api.secret}")
    private String clientSecret;

    @Bean(name = "naverRequestInterceptor")
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("X-Naver-Client-Id", clientId);
            requestTemplate.header("X-Naver-Client-Secret", clientSecret);
        };
    }

    @Bean
    public Encoder naverFeignEncoder() {
        return new JacksonEncoder();
    }
}
