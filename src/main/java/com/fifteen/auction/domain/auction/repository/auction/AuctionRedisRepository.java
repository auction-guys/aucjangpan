package com.fifteen.auction.domain.auction.repository.auction;

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
public class AuctionRedisRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TOP_BIDS_KEY_FORMAT = "auctions:%s:top-bids";

    public boolean isBidUnderPrice(String auctionSeq, Long bidPrice, int bidUnit) {
        Long currentPrice = findCurrentPrice(auctionSeq);

        return bidPrice < currentPrice + bidUnit;
    }

    public Long findBidCount(String auctionSeq) {
        String key = String.format(TOP_BIDS_KEY_FORMAT, auctionSeq);
        return redisTemplate.opsForZSet().zCard(key) - 1;
    }

    public Long findCurrentPrice(String auctionSeq) {
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
                    return splitValue[0]; // id_timestamp 형식에서 id를 추출
                })
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .map(Long::parseLong)
                .filter(aLong -> !aLong.equals(-1L)) // -1은 경매 시작가에 해당하는 value
                .toList();
    }

    public void flushTopBidCache(String auctionSeq) {
        String key = String.format(TOP_BIDS_KEY_FORMAT, auctionSeq);
        redisTemplate.delete(key);
    }
}
