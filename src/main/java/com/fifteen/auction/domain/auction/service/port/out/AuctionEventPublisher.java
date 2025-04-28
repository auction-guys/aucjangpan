package com.fifteen.auction.domain.auction.service.port.out;

import com.fifteen.auction.domain.auction.dto.event.BidRequestEvent;
import com.fifteen.auction.domain.auction.dto.event.BuyNowRequestEvent;

public interface AuctionEventPublisher {
    void publishBidRequest(BidRequestEvent event);

    void publishBuyNowRequest(BuyNowRequestEvent event);
}
