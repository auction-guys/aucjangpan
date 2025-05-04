package com.fifteen.auction.infra.redis.lock;

import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class LockRedissonManager implements LockManager{

    private final RedissonClient redissonClient;
    private static final String LOCK_KEY_PREFIX = "payment_lock:";
    private static final long WAIT_TIME = 5;
    private static final long LEASE_TIME = 2;

    @Override
    public <T> T executeWithLock(String key, Supplier<T> task) throws InterruptedException {
        String lockKey = LOCK_KEY_PREFIX + key;
        RLock lock = redissonClient.getFairLock(lockKey);

        log.info("락 획득 시도: {}", lockKey);

        if (lock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS)) {
            try {
                log.info("락 획득 성공: {}", lockKey);
                return task.get();
            } catch (Exception e) {
                log.error("작업 수행 중 예외 발생: {}", e.getMessage(), e);
                throw new ServerException(ErrorCode.PAYMENT_LOCK_EXCEPTION);
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                    log.info("락 해제: {}", lockKey);
                }
            }
        } else {
            log.warn("락 획득 실패: {}", lockKey);
            throw new ServerException(ErrorCode.PAYMENT_LOCK_ILLEGAL_STATE);
        }
    }
}
