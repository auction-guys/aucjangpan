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
    private Long currentPrice;
    private String auctionSeq;
    private Long bidCount;
    private Integer bidUnit;
//    private String thumbnailImgUrl;

    private Long buyNowPrice;

    private Boolean isBuyNowSet;
    private Boolean isAutoExtensible;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private LocalDateTime expiresAt;

    private Integer views;

    private MarketPriceFullResponse marketPrice;

    //조회수 views 추가
    private AuctionDetail(
            String productName, String auctionSeq, Integer bidUnit, Long buyNowPrice,
            Boolean isBuyNowSet, Boolean isAutoExtensible, LocalDateTime expiresAt,
            Integer views, MarketPriceFullResponse marketPrice
    ) {
        this.productName = productName;
        this.auctionSeq = auctionSeq;
        this.bidUnit = bidUnit;
        this.buyNowPrice = buyNowPrice;
        this.isBuyNowSet = isBuyNowSet;
        this.isAutoExtensible = isAutoExtensible;
        this.expiresAt = expiresAt;
        this.views = views;
        this.marketPrice = marketPrice;
    }

    public void updateBidInfo(Long currentPrice, Long bidCount) {
        this.currentPrice = currentPrice;
        this.bidCount = bidCount;
    }

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
