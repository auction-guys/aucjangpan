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
public class AuctionOpenEvent {
    private Long auctionId;
    private String auctionSeq;
    private LocalDateTime expiresAt;
    private Long startPrice;

    public static AuctionOpenEvent fromAuction(Auction auc) {
        return new AuctionOpenEvent(auc.getId(), auc.getAuctionSeq(), auc.getExpiresAt(), auc.getStartPrice());
    }
}
