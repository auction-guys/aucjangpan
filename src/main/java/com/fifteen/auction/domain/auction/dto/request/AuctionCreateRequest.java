package com.fifteen.auction.domain.auction.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class AuctionCreateRequest {
    @NotNull
    private Long productId;

    @Min(0) @NotNull
    private Long startPrice;

    @Min(0) @NotNull
    private Long buyNowPrice;

    @Min(0) @NotNull
    private Integer bidUnit;

    @NotNull
    private Boolean isBuyNowSet;

    @NotNull
    private Boolean isAutoExtensible;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private LocalDateTime expiresAt;

    private List<Long> tagIds;
}
