package com.fifteen.auction.domain.auction.dto.event;

import com.fifteen.auction.domain.auction.entity.Auction;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BuyNowEvent {
    private String auctionSeq;
    private Long winnerId;
    private LocalDateTime buyAt;
    private Long buyNowPrice;

    public static BuyNowEvent fromAuction(Auction auc) {
        return new BuyNowEvent(auc.getAuctionSeq(), auc.getWinnerId(), auc.getDoneAt(), auc.getBuyNowPrice());
    }
}
