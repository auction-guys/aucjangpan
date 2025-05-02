package com.fifteen.auction.infra.redis.lock;

import java.util.function.Supplier;

public interface LockManager {
    <T> T executeWithLock(String key, Supplier<T> task) throws InterruptedException;
}
