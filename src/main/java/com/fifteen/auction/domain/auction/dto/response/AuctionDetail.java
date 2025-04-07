package com.fifteen.auction.domain.auction.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fifteen.auction.domain.auction.entity.Auction;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuctionDetail {
    private String productName;
    //    private Long currentPrice;
    private String auctionSeq;
    //    private String bidCount;
    private Integer bidUnit;
//    private String thumbnailImgUrl;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private LocalDateTime expiresAt;

    public static AuctionDetail fromAuction(Auction auction) {
        return new AuctionDetail(
                auction.getProduct().getName(),
                auction.getAuctionSeq(),
                auction.getBidUnit(),
                auction.getExpiresAt()
        );
    }
}
