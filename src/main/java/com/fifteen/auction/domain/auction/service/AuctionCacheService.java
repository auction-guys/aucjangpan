package com.fifteen.auction.domain.auction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuctionCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TOP_BIDS_KEY_FORMAT = "auctions:%s:top-bids";

    public boolean isBidUnderPrice(String auctionSeq, Long bidPrice, int bidUnit) {
        Long currentPrice = getCurrentPrice(auctionSeq);

        return bidPrice < currentPrice + bidUnit;
    }

    public Long getCurrentPrice(String auctionSeq) {
        String key = String.format(TOP_BIDS_KEY_FORMAT, auctionSeq);

        Set<ZSetOperations.TypedTuple<Object>> response = redisTemplate
                .opsForZSet()
                .reverseRangeWithScores(key, 0, 0);
        
        return response.stream().findFirst()
                .map(o ->
                        o.getScore().longValue()).orElse(0L);
    }

    public void addNewHighPrice(String auctionSeq, Long bidderId, Long bidPrice) {
        long timestamp = System.currentTimeMillis();
        String key = String.format(TOP_BIDS_KEY_FORMAT, auctionSeq);
        String value = bidderId + "_" + timestamp;

        redisTemplate.opsForZSet().add(key, value, bidPrice);
    }

    public List<Long> findParticipants(String auctionSeq) {
        String key = String.format(TOP_BIDS_KEY_FORMAT, auctionSeq);

        Set<Object> response = redisTemplate.opsForZSet()
                .reverseRange(key, 0, -1);

        if (response == null) {
            return List.of();
        }

        return response
                .stream()
                .map(o -> {
                    String[] splitValue = ((String) o).split("_");
                    return splitValue[1];
                })
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .map(Long::parseLong)
                .toList();
    }

}
