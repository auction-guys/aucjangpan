package com.fifteen.auction.domain.auction.service.port.in;

import com.fifteen.auction.domain.auction.dto.event.BidProcessEvent;

public interface BidEventUseCase {

    void handleBidFromQueue(String auctionSeq, Long bidderId, Long bidPrice);

    void handleBidProcess(BidProcessEvent event);
}
