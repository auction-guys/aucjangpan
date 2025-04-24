package com.fifteen.auction.global.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    private static final String REDISSON_HOST_PREFIX = "redis://";

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress(REDISSON_HOST_PREFIX + redisHost + ":" + redisPort)
                .setConnectionMinimumIdleSize(30)   // 최소 유휴 연결 수
                .setConnectionPoolSize(100)         // 최대 연결 수
                .setRetryAttempts(3)                // 실패시 재시도 횟수
                .setRetryInterval(2000)             // 재시도 간격
                .setTimeout(3000)                   // redis 응답 타임아웃
                .setConnectTimeout(10000);          // redis 연결 타임아웃
        return Redisson.create(config);
    }
}
