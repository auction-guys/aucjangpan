package com.fifteen.auction.domain.auction.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fifteen.auction.domain.auction.entity.Bid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BidHistoryInfo {

    private Long bidderId;

    private String bidPrice;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "Asia/Seoul")
    private LocalDateTime bidAt;

    public static BidHistoryInfo forProgress(Bid bid) {
        return new BidHistoryInfo(bid.getBidderId(), "***", bid.getBidAt());
    }

    public static BidHistoryInfo forResult(Bid bid) {
        return new BidHistoryInfo(bid.getBidderId(), bid.getBidPrice().toString(), bid.getBidAt());
    }
}
