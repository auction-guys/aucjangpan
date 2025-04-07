package com.fifteen.auction.domain.auction.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AuctionCreateRequest {
    private Long productId;
    private Long startPrice;
    private Long buyNowPrice;
    private Integer bidUnit;
    private Boolean isBuyNowSet;
    private Boolean isAutoExtensible;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private LocalDateTime expiresAt;
}
