package com.fifteen.auction.domain.auction.service;

import com.fifteen.auction.domain.auction.dto.event.BidProcessEvent;
import com.fifteen.auction.domain.auction.dto.event.BuyNowProcessEvent;
import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.domain.auction.entity.Bid;
import com.fifteen.auction.domain.auction.repository.auction.AuctionRedisRepository;
import com.fifteen.auction.domain.auction.repository.auction.AuctionRepository;
import com.fifteen.auction.domain.auction.repository.bid.BidRepository;
import com.fifteen.auction.domain.auction.service.port.in.BidEventUseCase;
import com.fifteen.auction.domain.auction.util.ClockHolder;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class BidEventService implements BidEventUseCase {

    private final AuctionRedisRepository auctionRedisRepository;

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;

    private final ClockHolder clockHolder;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleBidProcess(BidProcessEvent event) {
        Auction auction = auctionRepository.findOpenOneByAuctionSeq(event.getAuctionSeq())
                .orElseThrow(() -> new ClientException(ErrorCode.AUCTION_NOT_FOUND));

        // 자동연장 처리
        auction.extendExpireTime(event.getBidAt());

        // 경매 표시가 반영
        auctionRedisRepository
                .addNewHighPrice(event.getAuctionSeq(), event.getBidderId(), event.getBidPrice());
    }

    @Override
    @Transactional
    public void handleBidFromQueue(String auctionSeq, Long bidderId, Long bidPrice) {
        Auction auc = auctionRepository.findOpenOneBySeqWithSeller(auctionSeq)
                .orElseThrow(() -> new ClientException(ErrorCode.AUCTION_NOT_FOUND));

        LocalDateTime bidAt = verifyAndGetRequestTime(auc.getExpiresAt(), ErrorCode.INVALID_BID_REQUEST);

        // bid price cache 체크
        if (auctionRedisRepository.isBidUnderPrice(auc.getAuctionSeq(), bidPrice, auc.getBidUnit())) {
            throw new ClientException(ErrorCode.LOW_BID_PRICE);
        }

        bidRepository.save(new Bid(auc, bidderId, bidPrice, bidAt));
        applicationEventPublisher.publishEvent(
                new BidProcessEvent(auctionSeq, bidderId, bidPrice, bidAt));
    }

    @Override
    @Transactional
    public void handleBuyNowFromQueue(String auctionSeq, Long bidderId) {
        Auction auc = auctionRepository.findOpenOneBySeqWithSeller(auctionSeq)
                .orElseThrow(() -> new ClientException(ErrorCode.AUCTION_NOT_FOUND));

        LocalDateTime buyAt = verifyAndGetRequestTime(auc.getExpiresAt(), ErrorCode.INVALID_BUY_NOW_REQUEST);

        auc.finalize(bidderId, auc.getBuyNowPrice(), buyAt);

        bidRepository.save(new Bid(auc, bidderId, auc.getBuyNowPrice(), buyAt));

        applicationEventPublisher.publishEvent(BuyNowProcessEvent.fromAuction(auc));
    }

    private LocalDateTime verifyAndGetRequestTime(LocalDateTime expiresAt, ErrorCode errorCode) {
        LocalDateTime buyAt = clockHolder.now();

        if (buyAt.isAfter(expiresAt)) {
            throw new ClientException(errorCode);
        }
        return buyAt;
    }
}
