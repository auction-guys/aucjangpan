package com.fifteen.auction.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class AsyncConfig {

    @Bean(name = "customExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);                                                        // 최소 스레드
        executor.setMaxPoolSize(2);                                                        // 최대 스레드
        executor.setQueueCapacity(0);                                                     // 작업 대기열 최대 크기
        executor.setAllowCoreThreadTimeOut(true);                                           // 유휴 상태가 아래 설정 지나면 스레드 제거
        executor.setKeepAliveSeconds(300);                                                  // 유휴 상태 시간 설정
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());    // 대기열 초과시 처리 방침
        executor.initialize();
        return executor;
    }

    @Bean(name = "customWebhookExecutor")
    public Executor tossExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(30);                                                        // 최소 스레드
        executor.setMaxPoolSize(60);                                                        // 최대 스레드
        executor.setQueueCapacity(100);                                                     // 작업 대기열 최대 크기
        executor.setAllowCoreThreadTimeOut(true);                                           // 유휴 상태가 아래 설정 지나면 스레드 제거
        executor.setKeepAliveSeconds(300);                                                  // 유휴 상태 시간 설정
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());    // 대기열 초과시 처리 방침
        executor.initialize();
        return executor;
    }
}
