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
public class BuyNowProcessEvent {
    private String auctionSeq;
    private Long winnerId;
    private LocalDateTime buyAt;
    private Long buyNowPrice;

    public static BuyNowProcessEvent fromAuction(Auction auc) {
        return new BuyNowProcessEvent(auc.getAuctionSeq(), auc.getWinnerId(), auc.getDoneAt(), auc.getBuyNowPrice());
    }
}
