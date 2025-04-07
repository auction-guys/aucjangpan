package com.fifteen.auction.domain.auction.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AuctionListItem {
    private String auctionSeq;
    private String productName;
    private Long currentPrice;
    private String sellerName;
    private Integer bidCount;
    private String productDesc;
//    private Long minMarketPrice;
//    private Long maxMarketPrice;
    private String thumbnailImgUrl;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private LocalDateTime expiresAt;
}
