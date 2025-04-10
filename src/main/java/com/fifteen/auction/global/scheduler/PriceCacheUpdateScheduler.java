package com.fifteen.auction.global.scheduler;


import com.fifteen.auction.domain.product.entity.Product;
import com.fifteen.auction.domain.product.repository.ProductRepository;
import com.fifteen.auction.domain.product.service.MarketPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
public class PriceCacheUpdateScheduler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductRepository productRepository;
    private final MarketPriceService marketPriceService;

    @Scheduled(fixedRate = 60_000) // 매 1분마다 실행
    public void refreshExpiredCacheProducts() {
        System.out.println("[스케줄러 동작 중] 캐시 점검중..");

        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            String cacheKey = "price:" + product.getId();
            Long ttl = redisTemplate.getExpire(cacheKey, TimeUnit.SECONDS);

            if (ttl == null || ttl <= 0) {
                marketPriceService.predictAndSavePrice(product);
                System.out.println("캐시 만료.  자동 재예측 성공 productId: " + product.getId());
            }
        }
    }
}
