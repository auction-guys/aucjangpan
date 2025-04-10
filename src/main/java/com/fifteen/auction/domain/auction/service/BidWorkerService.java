package com.fifteen.auction.domain.auction.service;

import com.fifteen.auction.domain.auction.dto.event.BidProcessEvent;
import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.domain.auction.repository.AuctionRepository;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Duration;
import java.time.LocalDateTime;

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
        if (auction.isAutoExtensible() && isWithinOneMinute(auction.getExpiresAt(), event.getBidAt())) {
            auction.extendExpireTime();
            System.out.println(auction.getExpiresAt());
        }

        // 경매 표시가 반영
        auctionCacheService
                .addNewHighPrice(event.getAuctionSeq(), event.getBidderId(), event.getBidPrice());

        // 입찰 이력 캐싱
        auctionCacheService.addToBidHistory(event.getAuctionSeq(), event.getBidderId());
    }

    private boolean isWithinOneMinute(LocalDateTime expiresAt, LocalDateTime bidAt) {
        Duration between = Duration.between(bidAt, expiresAt).abs();
        return between.toMinutes() == 0 && between.getSeconds() <= 60;
    }
}
