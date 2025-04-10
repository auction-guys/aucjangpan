package com.fifteen.auction.domain.auction.service;

import com.fifteen.auction.domain.auction.dto.event.BidProcessEvent;
import com.fifteen.auction.domain.auction.dto.request.DoBidRequest;
import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.domain.auction.entity.AuctionStatus;
import com.fifteen.auction.domain.auction.entity.Bid;
import com.fifteen.auction.domain.auction.repository.AuctionRepository;
import com.fifteen.auction.domain.auction.repository.BidRepository;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BidService {

    private final ApplicationEventPublisher applicationEventPublisher;

    private final AuctionCacheService auctionCacheService;

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;


    @Transactional
    public Long bid(String auctionSeq, Long userId, DoBidRequest req) {

        LocalDateTime bidAt = LocalDateTime.now();

        // 본인이 생성한 경매이거나 마감된 경매인지 체크
        Auction findAuction = auctionRepository.findOpenOneBySeqWithSeller(auctionSeq)
                .orElseThrow(() -> new ClientException(ErrorCode.AUCTION_NOT_FOUND));

        if (isInvalidBid(userId, findAuction, bidAt)) {
            throw new ClientException(ErrorCode.INVALID_BID_REQUEST);
        }

        // bid price cache 체크
        if (auctionCacheService.isBidUnderPrice(auctionSeq, req.getBidPrice(), findAuction.getBidUnit())) {
            throw new ClientException(ErrorCode.LOW_BID_PRICE);
        }

        // 이벤트 던짐
        applicationEventPublisher.publishEvent(
                new BidProcessEvent(auctionSeq, userId, req.getBidPrice(), bidAt));

        // bid commit
        return bidRepository
                .save(new Bid(findAuction, userId, req.getBidPrice(), bidAt))
                .getId();
    }

    private boolean isInvalidBid(Long userId, Auction findAuction, LocalDateTime bidAt) {
        return userId.equals(findAuction.getProduct().getSeller().getId())
                || findAuction.getStatus() != AuctionStatus.OPEN
                || bidAt.isAfter(findAuction.getExpiresAt());
    }
}
