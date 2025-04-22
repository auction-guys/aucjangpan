package com.fifteen.auction.domain.auction.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class AuctionUpdateRequest {
    @Min(0)
    private Long startPrice;

    @Min(0)
    private Long buyNowPrice;

    @Min(0)
    private Integer bidUnit;

    private Boolean isBuyNowSet;

    private Boolean isAutoExtensible;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private LocalDateTime expiresAt;
}
