package com.fifteen.auction.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.script.RedisScript;

@Configuration
public class LuaConfig {

    @Bean
    public RedisScript<Long> getCurrentPriceScript() {
        Resource classPathResource = new ClassPathResource("scripts/auction/get-current-price.lua");
        return RedisScript.of(classPathResource, Long.class);
    }

    @Bean
    public RedisScript<Void> renewHighPriceScript() {
        Resource classPathResource = new ClassPathResource("scripts/auction/renew-high-price.lua");
        return RedisScript.of(classPathResource, Void.class);
    }
}
