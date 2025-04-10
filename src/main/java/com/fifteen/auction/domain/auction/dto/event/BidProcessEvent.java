package com.fifteen.auction.domain.auction.dto.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class BidProcessEvent {

    private String auctionSeq;
    private Long bidderId;
    private Long bidPrice;
    private LocalDateTime bidAt;
}
