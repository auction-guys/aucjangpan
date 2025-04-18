package com.fifteen.auction.domain.auction.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class BidRequest {

    @Min(0) @NotNull
    private Long price;
}
