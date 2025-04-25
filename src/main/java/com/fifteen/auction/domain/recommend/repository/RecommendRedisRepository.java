package com.fifteen.auction.domain.recommend.repository;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Component
@Repository
@RequiredArgsConstructor
public class RecommendRedisRepository {

    private final StringRedisTemplate redisTemplate;
    private static final Duration RECOMMEND_TTL = Duration.ofHours(1);

    private static final String RECOMMEND_KEY_PREFIX = "recommend:group:";

    public void saveRecommendation(Long groupId, Long auctionId, int score) {
        String key = RECOMMEND_KEY_PREFIX + groupId;
        redisTemplate.opsForZSet().add(key, auctionId.toString(), score);
    }

    public void saveRecommendations(Long groupId, List<AuctionScore> recommendations) {
        String key = RECOMMEND_KEY_PREFIX + groupId;

        redisTemplate.delete(key); // 기존 삭제

        for (AuctionScore r : recommendations) {
            redisTemplate.opsForZSet().add(key, r.auctionId().toString(), r.score());
        }
        redisTemplate.expire(key, RECOMMEND_TTL); // TTL 설정
    }

    public Set<ZSetOperations.TypedTuple<String>> getTopRecommendations(Long groupId, int limit) {
        String key = RECOMMEND_KEY_PREFIX + groupId;
        return redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, limit - 1);
    }

    public void deleteRecommendations(Long groupId) {
        String key = RECOMMEND_KEY_PREFIX + groupId;
        redisTemplate.delete(key);
    }

    public record AuctionScore(Long auctionId, int score) {}
}