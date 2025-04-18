package com.fifteen.auction.domain.auction.service;

import com.fifteen.auction.domain.auction.dto.event.BidProcessEvent;
import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.domain.auction.repository.auction.AuctionRepository;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class BidWorkerService {

    private final AuctionCacheService auctionCacheService;

    private final AuctionRepository auctionRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleBidProcess(BidProcessEvent event) {
        Auction auction = auctionRepository.findOpenOneByAuctionSeq(event.getAuctionSeq())
                .orElseThrow(() -> new ClientException(ErrorCode.AUCTION_NOT_FOUND));

        // 자동연장 처리
        auction.extendExpireTime(event.getBidAt());

        // 경매 표시가 반영
        auctionCacheService
                .addNewHighPrice(event.getAuctionSeq(), event.getBidderId(), event.getBidPrice());
    }
}
