package com.fifteen.auction.domain.auction.service.port.in;

public interface BidEventHandler {

    void handleBidFromQueue(String auctionSeq, Long bidderId, Long bidPrice, int bidUnit);

    void handleBuyNowFromQueue(String auctionSeq, Long bidderId);
}
