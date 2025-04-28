package com.fifteen.auction.domain.auction.dto.event;

import com.fifteen.auction.domain.auction.entity.Auction;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class AuctionExpirationEvent {
    private Long auctionId;
    private String auctionSeq;
    private Long startPrice;

    public static AuctionExpirationEvent fromAuction(Auction auc) {
        return new AuctionExpirationEvent(auc.getId(), auc.getAuctionSeq(), auc.getStartPrice());
    }
}
