package com.fifteen.auction.domain.auction.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.domain.product.dto.response.MarketPriceFullResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuctionDetail {
    private String productName;
    //    private Long currentPrice;
    private String auctionSeq;
    //    private String bidCount;
    private Integer bidUnit;
//    private String thumbnailImgUrl;

    private Long buyNowPrice;

    private Boolean isBuyNowSet;
    private Boolean isAutoExtensible;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private LocalDateTime expiresAt;

    private Integer views;

    private MarketPriceFullResponse marketPrice;

    public static AuctionDetail fromAuction(Auction auction, MarketPriceFullResponse marketPrice) {
        return new AuctionDetail(
                auction.getProduct().getName(),
                auction.getAuctionSeq(),
                auction.getBidUnit(),
                auction.getBuyNowPrice(),
                auction.isBuyNowSet(),
                auction.isAutoExtensible(),
                auction.getExpiresAt(),
                auction.getViews(),
                marketPrice
        );
    }
}
