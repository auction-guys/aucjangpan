package com.fifteen.auction.domain.auction.dto.response;

import com.fifteen.auction.domain.auction.dto.constant.BidResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BidRequestResult {
    private boolean success;
    private String reason;

    public static BidRequestResult of(BidResult result) {
        return new BidRequestResult(result.isSucceed(), result.getMessage());
    }
}
