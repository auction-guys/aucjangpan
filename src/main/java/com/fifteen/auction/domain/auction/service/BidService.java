package com.fifteen.auction.domain.auction.service;

import com.fifteen.auction.domain.auction.dto.event.BidProcessEvent;
import com.fifteen.auction.domain.auction.dto.event.BidRequestEvent;
import com.fifteen.auction.domain.auction.dto.event.BuyNowEvent;
import com.fifteen.auction.domain.auction.dto.request.BidRequest;
import com.fifteen.auction.domain.auction.dto.response.BidHistoryInfo;
import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.domain.auction.entity.Bid;
import com.fifteen.auction.domain.auction.repository.auction.AuctionRedisRepository;
import com.fifteen.auction.domain.auction.repository.auction.AuctionRepository;
import com.fifteen.auction.domain.auction.repository.bid.BidRepository;
import com.fifteen.auction.domain.auction.service.port.out.AuctionEventPublisher;
import com.fifteen.auction.domain.auction.util.ClockHolder;
import com.fifteen.auction.global.dto.PageCond;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.fifteen.auction.domain.user.enums.UserRole.Authority.ROLE_USER;

@Service
@RequiredArgsConstructor
public class BidService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final AuctionEventPublisher auctionEventPublisher;
    private final ClockHolder clockHolder;

    private final AuctionRedisRepository auctionRedisRepository;

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;

    @Secured(ROLE_USER)
    @Transactional
    public void bid(String auctionSeq, Long userId, BidRequest req) {

        Auction findAuction = auctionRepository.findOpenOneBySeqWithSeller(auctionSeq)
                .orElseThrow(() -> new ClientException(ErrorCode.AUCTION_NOT_FOUND));

        if (findAuction.isOwnedByUser(userId)) {
            throw new ClientException(ErrorCode.INVALID_BID_REQUEST);
        }

        LocalDateTime bidAt = clockHolder.now();

        if (bidAt.isAfter(findAuction.getExpiresAt())) {
            throw new ClientException(ErrorCode.INVALID_BID_REQUEST);
        }

        // bid price cache 체크
        if (auctionRedisRepository.isBidUnderPrice(auctionSeq, req.getPrice(), findAuction.getBidUnit())) {
            throw new ClientException(ErrorCode.LOW_BID_PRICE);
        }

        bidRepository.save(new Bid(findAuction, userId, req.getPrice(), bidAt));

        applicationEventPublisher.publishEvent(
                new BidProcessEvent(auctionSeq, userId, req.getPrice(), bidAt));
    }

    @Secured(ROLE_USER)
    @Transactional
    public void buyNow(String auctionSeq, Long userId) {
        Auction findAuction = auctionRepository.findOpenOneBySeqWithSeller(auctionSeq)
                .orElseThrow(() -> new ClientException(ErrorCode.AUCTION_NOT_FOUND));

        if (!findAuction.isBuyNowSet()) {
            throw new ClientException(ErrorCode.CANNOT_BUY_NOW);
        }

        if (findAuction.isOwnedByUser(userId)) {
            throw new ClientException(ErrorCode.INVALID_BUY_NOW_REQUEST);
        }

        LocalDateTime buyAt = clockHolder.now();

        if (buyAt.isAfter(findAuction.getExpiresAt())) {
            throw new ClientException(ErrorCode.INVALID_BUY_NOW_REQUEST);
        }

        findAuction.finalize(userId, findAuction.getBuyNowPrice(), buyAt);

        bidRepository.save(new Bid(findAuction, userId, findAuction.getBuyNowPrice(), buyAt));

        applicationEventPublisher.publishEvent(BuyNowEvent.fromAuction(findAuction));
    }

    @Secured(ROLE_USER)
    @Transactional(readOnly = true)
    public Page<BidHistoryInfo> bidHistoriesInProgress(String auctionSeq, PageCond cond) {
        Pageable pageRequest = PageRequest.of(cond.getPageNum() - 1, cond.getPageSize());
        return bidRepository.findAllInProgressByAuctionSeq(pageRequest, auctionSeq);
    }

    public void putBidIntoQueue(String auctionSeq, Long userId, Long bidPrice) {
        Auction findAuction = auctionRepository.findOpenOneBySeqWithSeller(auctionSeq)
                .orElseThrow(() -> new ClientException(ErrorCode.AUCTION_NOT_FOUND));

        if (findAuction.isOwnedByUser(userId)) {
            throw new ClientException(ErrorCode.INVALID_BID_REQUEST);
        }

        if (auctionRedisRepository.isBidUnderPrice(auctionSeq, bidPrice, findAuction.getBidUnit())) {
            throw new ClientException(ErrorCode.LOW_BID_PRICE);
        }

        auctionEventPublisher.publishBidRequest(new BidRequestEvent(auctionSeq, userId, bidPrice));
    }
}
