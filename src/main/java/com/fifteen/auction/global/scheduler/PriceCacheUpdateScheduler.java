package com.fifteen.auction.global.scheduler;

import com.fifteen.auction.domain.product.entity.Product;
import com.fifteen.auction.domain.product.repository.ProductRepository;
import com.fifteen.auction.domain.product.service.MarketPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.Optional;
import java.util.Collections;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class PriceCacheUpdateScheduler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductRepository productRepository;
    private final MarketPriceService marketPriceService;

    @Scheduled(fixedRate = 60_000) // 1분마다 실행
    public void refreshExpiredCacheProducts() {
        System.out.println("[스케줄러 동작 중] 캐시 점검중..");

        // 1. Redis에서 캐싱된 productId 들 가져옴
        Set<String> keys = redisTemplate.keys("price:*");
        Set<Long> cachedIds = Optional.ofNullable(keys)
                .orElse(Collections.emptySet())
                .stream()
                .map(k -> k.replace("price:", ""))
                .map(Long::parseLong)
                .collect(Collectors.toSet());

        // 2. 캐싱 안된 상품만 조회
        List<Product> uncachedProducts = cachedIds.isEmpty() ?
                productRepository.findAll() :
                productRepository.findByIdNotIn(cachedIds);

        // 3. 캐싱 안된 상품만 예측해서 캐싱
        for (Product product : uncachedProducts) {
            try {
                marketPriceService.predictAndSavePrice(product);
                System.out.println("캐시 없음! 자동 예측 완료 - productId: " + product.getId());
            } catch (Exception e) {
                System.err.println("예측 실패 - productId: " + product.getId());
                e.printStackTrace();
            }
        }
    }
}
