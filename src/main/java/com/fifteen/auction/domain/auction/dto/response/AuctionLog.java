package com.fifteen.auction.domain.auction.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AuctionLog {
    private String auctionSeq;
    private String thumbnailImgUrl;
    private Long currentPrice;
    private String sellerName;
    private Integer bidRank;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private LocalDateTime expiresAt;
}
