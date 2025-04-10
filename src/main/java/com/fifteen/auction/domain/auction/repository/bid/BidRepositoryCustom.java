package com.fifteen.auction.domain.auction.repository.bid;

import com.fifteen.auction.domain.auction.dto.response.BidHistoryInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BidRepositoryCustom {
    Page<BidHistoryInfo> findAllInProgressByAuctionSeq(Pageable pageable, String auctionSeq);
}
