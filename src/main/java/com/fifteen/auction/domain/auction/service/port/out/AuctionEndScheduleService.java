package com.fifteen.auction.domain.auction.service.port.out;

import com.fifteen.auction.domain.auction.dto.event.AuctionOpenEvent;

public interface AuctionSchedulerService {
    void scheduleAuctionEnd(AuctionOpenEvent e);

    void cancelScheduleAuctionEnd(String auctionSeq);
}
