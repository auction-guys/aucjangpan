package com.fifteen.auction.domain.auction.service;

import com.fifteen.auction.domain.auction.dto.constant.BidResult;
import com.fifteen.auction.domain.auction.dto.event.BidProcessEvent;
import com.fifteen.auction.domain.auction.dto.event.BidRequestEvent;
import com.fifteen.auction.domain.auction.dto.event.BuyNowProcessEvent;
import com.fifteen.auction.domain.auction.dto.event.BuyNowRequestEvent;
import com.fifteen.auction.domain.auction.dto.request.BidRequest;
import com.fifteen.auction.domain.auction.dto.response.BidHistoryInfo;
import com.fifteen.auction.domain.auction.dto.response.BidRequestResult;
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

        verifyAuctionOwnerShip(userId, findAuction, ErrorCode.INVALID_BID_REQUEST);

        LocalDateTime bidAt = verifyAndGetRequestTime(
                findAuction.getExpiresAt(), ErrorCode.INVALID_BID_REQUEST);

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

        verifyAuctionOwnerShip(userId, findAuction, ErrorCode.INVALID_BUY_NOW_REQUEST);

        LocalDateTime buyAt = verifyAndGetRequestTime(
                findAuction.getExpiresAt(), ErrorCode.INVALID_BUY_NOW_REQUEST);

        findAuction.finalize(userId, findAuction.getBuyNowPrice(), buyAt);

        bidRepository.save(new Bid(findAuction, userId, findAuction.getBuyNowPrice(), buyAt));

        applicationEventPublisher.publishEvent(BuyNowProcessEvent.fromAuction(findAuction));
    }

    @Secured(ROLE_USER)
    @Transactional(readOnly = true)
    public Page<BidHistoryInfo> bidHistoriesInProgress(String auctionSeq, PageCond cond) {
        Pageable pageRequest = PageRequest.of(cond.getPageNum() - 1, cond.getPageSize());
        return bidRepository.findAllInProgressByAuctionSeq(pageRequest, auctionSeq);
    }

    /**
     * Bid V2 Methods
     */

    @Secured(ROLE_USER)
    public void putBidIntoQueue(String auctionSeq, Long userId, Long bidPrice) {
        Auction findAuction = auctionRepository.findOpenOneBySeqWithSeller(auctionSeq)
                .orElseThrow(() -> new ClientException(ErrorCode.AUCTION_NOT_FOUND));

        verifyAuctionOwnerShip(userId, findAuction, ErrorCode.INVALID_BID_REQUEST);

        if (auctionRedisRepository.isBidUnderPrice(auctionSeq, bidPrice, findAuction.getBidUnit())) {
            throw new ClientException(ErrorCode.LOW_BID_PRICE);
        }

        auctionEventPublisher.publishBidRequest(new BidRequestEvent(auctionSeq, userId, bidPrice, findAuction.getBidUnit()));
    }


    @Secured(ROLE_USER)
    @Transactional
    public void putBuyNowIntoQueue(String auctionSeq, Long userId) {
        Auction findAuction = auctionRepository.findOpenOneBySeqWithSeller(auctionSeq)
                .orElseThrow(() -> new ClientException(ErrorCode.AUCTION_NOT_FOUND));

        if (!findAuction.isBuyNowSet()) {
            throw new ClientException(ErrorCode.CANNOT_BUY_NOW);
        }

        verifyAuctionOwnerShip(userId, findAuction, ErrorCode.INVALID_BUY_NOW_REQUEST);

        auctionEventPublisher.publishBuyNowRequest(
                new BuyNowRequestEvent(auctionSeq, userId, findAuction.getBuyNowPrice()));
    }

    /**
     * Bid V3 Methods
     */

    @Secured(ROLE_USER)
    public BidRequestResult putBidIntoQueueV2(String auctionSeq, Long userId, Long bidPrice) {
        Auction findAuction = auctionRepository.findOpenOneBySeqWithSeller(auctionSeq).orElse(null);

        if (findAuction == null) {
            return BidRequestResult.of(BidResult.CANNOT_FIND_AUCTION);
        }

        if (findAuction.isOwnedByUser(userId)) {
            return BidRequestResult.of(BidResult.UNBIDDABLE_AUCTION);
        }

        if (auctionRedisRepository.isBidUnderPrice(auctionSeq, bidPrice, findAuction.getBidUnit())) {
            return BidRequestResult.of(BidResult.LOW_BID_PRICE);
        }

        auctionEventPublisher.publishBidRequest(new BidRequestEvent(auctionSeq, userId, bidPrice, findAuction.getBidUnit()));

        return BidRequestResult.of(BidResult.BID_SUCCEED);
    }


    @Secured(ROLE_USER)
    public BidRequestResult putBuyNowIntoQueueV2(String auctionSeq, Long userId) {
        Auction findAuction = auctionRepository.findOpenOneBySeqWithSeller(auctionSeq).orElse(null);

        if (findAuction == null) {
            return BidRequestResult.of(BidResult.CANNOT_FIND_AUCTION);
        }

        if (findAuction.isOwnedByUser(userId)) {
            return BidRequestResult.of(BidResult.UNBIDDABLE_AUCTION);
        }

        if (!findAuction.isBuyNowSet()) {
            return BidRequestResult.of(BidResult.CANNOT_BUY_NOW);
        }

        auctionEventPublisher.publishBuyNowRequest(
                new BuyNowRequestEvent(auctionSeq, userId, findAuction.getBuyNowPrice()));

        return BidRequestResult.of(BidResult.BUY_NOW_SUCCEED);
    }

    @Transactional
    public void settleBid(String auctionSeq, Long bidderId, Long bidPrice) {
        Auction auc = auctionRepository.findOpenOneBySeqWithSeller(auctionSeq)
                .orElseThrow(() -> new ClientException(ErrorCode.AUCTION_NOT_FOUND));

        LocalDateTime bidAt = verifyAndGetRequestTime(auc.getExpiresAt(), ErrorCode.INVALID_BID_REQUEST);

        bidRepository.save(new Bid(auc, bidderId, bidPrice, bidAt));

        // 자동연장 처리
        auc.extendExpireTime(bidAt);
    }

    /**
     * private method
     */

    private LocalDateTime verifyAndGetRequestTime(LocalDateTime expiresAt, ErrorCode errorCode) {
        LocalDateTime buyAt = clockHolder.now();

        if (buyAt.isAfter(expiresAt)) {
            throw new ClientException(errorCode);
        }
        return buyAt;
    }

    private void verifyAuctionOwnerShip(Long userId, Auction findAuction, ErrorCode errorCode) {
        if (findAuction.isOwnedByUser(userId)) {
            throw new ClientException(errorCode);
        }
    }
}
