package com.fifteen.auction.domain.auction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuctionCacheService {

    public static final int RETAIN_RANK_SIZE = 2;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TOP_BIDS_KEY_FORMAT = "auctions:%s:top-bids";
    private static final String BID_HISTORY_KEY_FORMAT = "auctions:%s:bid-history";

    private final RedisScript<Long> getCurrentPriceScript;
    private final RedisScript<Void> renewHighPriceScript;

    public boolean isBidUnderPrice(String auctionSeq, Long bidPrice, int bidUnit) {
        Long currentPrice = getCurrentPrice(auctionSeq);

        return bidPrice < currentPrice + bidUnit;
    }

    public Long getCurrentPrice(String auctionSeq) {
        String key = String.format(TOP_BIDS_KEY_FORMAT, auctionSeq);

        return redisTemplate
                .execute(getCurrentPriceScript, List.of(key));
    }

    public void addNewHighPrice(String auctionSeq, Long bidderId, Long bidPrice) {
        long timestamp = System.currentTimeMillis();
        String key = String.format(TOP_BIDS_KEY_FORMAT, auctionSeq);

        redisTemplate.execute(
                renewHighPriceScript,
                List.of(key),
                bidderId, bidPrice, timestamp, RETAIN_RANK_SIZE
        );
    }

    public void addToBidHistory(String auctionSeq, Long bidderId) {
        String key = String.format(BID_HISTORY_KEY_FORMAT, auctionSeq);
        redisTemplate.opsForSet().add(key, bidderId);
    }
}
