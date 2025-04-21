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
        executor.setCorePoolSize(3);                                                        // 최소 스레드
        executor.setMaxPoolSize(50);                                                        // 최대 스레드
        executor.setQueueCapacity(100);                                                     // 작업 대기열 최대 크기
        //TODO: 로그 컨벤션 어떻게 하지 정하고 다시 그에 맞춰 수정 후 주석 취소
//        executor.setThreadNamePrefix("async-settlement-");                                  // 스레드 이름 접두어 설정
        executor.setAllowCoreThreadTimeOut(true);                                           // 유휴 상태가 아래 설정 지나면 스레드 제거
        executor.setKeepAliveSeconds(300);                                                  // 유휴 상태 시간 설정
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());    // 대기열 초과시 처리 방침
        executor.initialize();
        return executor;
    }
}
