package com.fifteen.auction.domain.auction.dto.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class BidRequestEvent {
    private String auctionSeq;
    private Long userId;
    private Long bidPrice;
    private int bidUnit;
}
