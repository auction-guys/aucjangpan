package com.fifteen.auction.global.config;

import feign.Logger;
import feign.codec.Encoder;
import feign.form.FormEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public Encoder feignFormEncoder() {
        return new FormEncoder();
    }

    @Bean
    public Logger feignLogger() {
        return new Logger.JavaLogger().appendToFile("feign.log");
    }
}