package com.fifteen.auction.domain.auction.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.domain.auction.entity.AuctionStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuctionLog {
    private String auctionSeq;
    private String thumbnailImgUrl;
    private Long price;
    private String productName;
    private String sellerName;
    private Boolean isWinner;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private LocalDateTime expiredTime;
    private AuctionStatus status;


    public static AuctionLog fromAuction(Auction auc, Long userId, Long currentPrice) {
        return new AuctionLog(
                auc.getAuctionSeq(),
                auc.getProduct().getThumbnailUrl(),
                currentPrice,
                auc.getProduct().getName(),
                auc.getProduct().getSeller().getName(),
                userId.equals(auc.getWinnerId()),
                auc.getDoneAt() == null ? auc.getExpiresAt() : auc.getDoneAt(),
                auc.getStatus()
        );
    }
}
