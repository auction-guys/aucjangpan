package com.fifteen.auction.domain.auction.service.port.in;

public interface AuctionScheduleProcessor {

    void processExpired(Long auctionId, String auctionSeq, Long startPrice);

    void processBuyNowMessage(String auctionSeq, Long winnerId);
}
