package com.fifteen.auction.domain.recommend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    public void saveRecommendationScore(Long groupId, Map<Long, Integer> auctionScoreMap) {
        String redisKey = "recommend:group:" + groupId;
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        // 기존 추천 데이터 삭제
        redisTemplate.delete(redisKey);

        //새 추천 점수 저장
        for (Map.Entry<Long, Integer> entry : auctionScoreMap.entrySet()) {
            Long auctionId = entry.getKey();
            Integer score = entry.getValue();
            zSetOps.add(redisKey, String.valueOf(auctionId), score);
        }
    }

    public boolean isViewedRecently(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }


    public void markViewed(String key, Duration ttl) {
        redisTemplate.opsForValue().set(key, "viewed", ttl);
    }
}

