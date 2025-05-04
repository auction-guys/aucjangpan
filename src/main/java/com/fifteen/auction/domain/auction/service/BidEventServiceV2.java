package com.fifteen.auction.domain.auction.service;

import com.fifteen.auction.domain.auction.dto.event.BuyNowV2ProcessEvent;
import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.domain.auction.entity.Bid;
import com.fifteen.auction.domain.auction.repository.auction.AuctionRedisRepository;
import com.fifteen.auction.domain.auction.repository.auction.AuctionRepository;
import com.fifteen.auction.domain.auction.repository.bid.BidRepository;
import com.fifteen.auction.domain.auction.service.port.in.BidEventHandler;
import com.fifteen.auction.domain.auction.util.ClockHolder;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

//@Component
@RequiredArgsConstructor
public class BidEventServiceV2 implements BidEventHandler {

    private final AuctionRedisRepository auctionRedisRepository;

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final BidService bidService;

    private final ClockHolder clockHolder;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void handleBidFromQueue(String auctionSeq, Long bidderId, Long bidPrice, int bidUnit) {
        // bid price cache 체크
        if (auctionRedisRepository.isBidUnderPrice(auctionSeq, bidPrice, bidUnit)) {
            throw new ClientException(ErrorCode.LOW_BID_PRICE);
        }

        bidService.settleBid(auctionSeq, bidderId, bidPrice);

        // 경매 표시가 반영
        auctionRedisRepository
                .addNewHighPrice(auctionSeq, bidderId, bidPrice);
    }

    @Override
    @Transactional
    public void handleBuyNowFromQueue(String auctionSeq, Long bidderId) {
        Auction auc = auctionRepository.findOpenOneBySeqWithSeller(auctionSeq)
                .orElseThrow(() -> new ClientException(ErrorCode.AUCTION_NOT_FOUND));

        LocalDateTime buyAt = verifyAndGetRequestTime(auc.getExpiresAt(), ErrorCode.INVALID_BUY_NOW_REQUEST);

        auc.finalize(bidderId, auc.getBuyNowPrice(), buyAt);

        bidRepository.save(new Bid(auc, bidderId, auc.getBuyNowPrice(), buyAt));

        applicationEventPublisher.publishEvent(BuyNowV2ProcessEvent.fromAuction(auc));
    }

    private LocalDateTime verifyAndGetRequestTime(LocalDateTime expiresAt, ErrorCode errorCode) {
        LocalDateTime buyAt = clockHolder.now();

        if (buyAt.isAfter(expiresAt)) {
            throw new ClientException(errorCode);
        }
        return buyAt;
    }
}
