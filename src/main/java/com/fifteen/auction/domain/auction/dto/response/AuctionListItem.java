package com.fifteen.auction.domain.auction.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fifteen.auction.domain.auction.entity.Auction;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AuctionListItem {
    private String auctionSeq;
    private String productName;
    private Long currentPrice;
    private String sellerName;
    private Long bidCount;
    private String productDesc;
    //    private Long minMarketPrice;
//    private Long maxMarketPrice;
//    private String thumbnailImgUrl;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private LocalDateTime expiresAt;

    private AuctionListItem(
            String auctionSeq, String productName, String sellerName,
            String productDesc, LocalDateTime expiresAt
    ) {
        this.auctionSeq = auctionSeq;
        this.productName = productName;
        this.sellerName = sellerName;
        this.productDesc = productDesc;
        this.expiresAt = expiresAt;
    }

    public void updateBidInfo(Long currentPrice, Long bidCount) {
        this.currentPrice = currentPrice;
        this.bidCount = bidCount;
    }

    public static AuctionListItem fromAuction(Auction auction) {
        return new AuctionListItem(
                auction.getAuctionSeq(),
                auction.getProduct().getName(),
                auction.getProduct().getSeller().getName(),
                auction.getProduct().getDescription(),
                auction.getExpiresAt()
        );
    }
}
