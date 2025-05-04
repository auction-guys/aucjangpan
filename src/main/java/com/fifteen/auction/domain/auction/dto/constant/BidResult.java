package com.fifteen.auction.domain.auction.dto.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BidResult {
    BID_SUCCEED(true, "입찰 요청에 성공했습니다."),
    BUY_NOW_SUCCEED(true, "즉시 구매 요청에 성공했습니다."),
    LOW_BID_PRICE(false, "현재 입찰할 수 있는 가격보다 낮은 입찰입니다. 가격을 높여 입찰해주세요."),
    UNBIDDABLE_AUCTION(false, "자신이 생성한 경매에는 참여할 수 있습니다."),
    CANNOT_FIND_AUCTION(false, "해당 경매는 진행중인 경매가 아닙니다."),
    CANNOT_BUY_NOW(false, "즉시 구매가 불가능한 경매입니다.");

    private final boolean succeed;
    private final String message;
}
