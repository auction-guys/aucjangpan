package com.fifteen.auction.domain.auction.service.port.out;

import com.fifteen.auction.domain.auction.dto.event.BidRequestEvent;
import org.springframework.stereotype.Component;

@Component
public interface AuctionEventPublisher {
    void publishBidRequest(BidRequestEvent event);
}
