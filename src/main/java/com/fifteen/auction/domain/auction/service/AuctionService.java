package com.fifteen.auction.domain.auction.service;

import com.fifteen.auction.domain.auction.dto.event.AuctionOpenEvent;
import com.fifteen.auction.domain.auction.dto.request.AuctionCreateRequest;
import com.fifteen.auction.domain.auction.dto.request.AuctionUpdateRequest;
import com.fifteen.auction.domain.auction.dto.response.AuctionDetail;
import com.fifteen.auction.domain.auction.dto.response.AuctionListItem;
import com.fifteen.auction.domain.auction.dto.response.AuctionLog;
import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.domain.auction.repository.auction.AuctionRepository;
import com.fifteen.auction.domain.auction.repository.bid.BidRepository;
import com.fifteen.auction.domain.auction.util.AuctionSeqGenerator;
import com.fifteen.auction.domain.product.dto.response.MarketPriceFullResponse;
import com.fifteen.auction.domain.product.entity.Product;
import com.fifteen.auction.domain.product.repository.ProductRepository;
import com.fifteen.auction.domain.product.service.MarketPriceService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.fifteen.auction.domain.user.enums.UserRole.Authority.ROLE_USER;

@Service
@RequiredArgsConstructor
public class AuctionService {
    private final AuctionRepository auctionRepository;
    private final ProductRepository productRepository;
    private final BidRepository bidRepository;

    private final MarketPriceService marketPriceService;
    private final AuctionCacheService auctionCacheService;

    private final AuctionSeqGenerator auctionSeqGenerator;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Secured(ROLE_USER)
    @Transactional
    public String create(AuctionCreateRequest req, Long userId) {
        Product product = productRepository.findByIdWithSeller(req.getProductId())
                .orElseThrow(() -> new ClientException(ErrorCode.PRODUCT_NOT_FOUND));

        // 자신이 생성한 물품인지 확인
        if (!product.isUserASeller(userId)) {
            throw new ClientException(ErrorCode.NOT_OWNING_PRODUCT);
        }

        String auctionSeq = auctionSeqGenerator.generate(LocalDate.now());

        Auction auction = new Auction(product, auctionSeq,
                req.getStartPrice(), req.getBuyNowPrice(), req.getBidUnit(),
                req.getIsBuyNowSet(), req.getIsAutoExtensible(), req.getExpiresAt());

        return auctionRepository.save(auction).getAuctionSeq();
    }

    @Secured(ROLE_USER)
    @Transactional
    public void cancel(String auctionSeq, Long sellerId) {
        Auction auction = auctionRepository
                .findOneBySeqAndSellerId(auctionSeq, sellerId)
                .orElseThrow(() -> new ClientException(ErrorCode.AUCTION_NOT_FOUND));
        auction.cancel(LocalDateTime.now());
    }

    @Secured(ROLE_USER)
    @Transactional
    public void open(String auctionSeq, Long sellerId) {
        Auction auction = auctionRepository
                .findOneBySeqAndSellerId(auctionSeq, sellerId)
                .orElseThrow(() -> new ClientException(ErrorCode.AUCTION_NOT_FOUND));

        auction.open();
        auctionCacheService.addNewHighPrice(auctionSeq, -1L, auction.getStartPrice());

        // 경매가 개시되면 이벤트 등록 -> 마감 메시지 스케줄링
        applicationEventPublisher.publishEvent(AuctionOpenEvent.fromAuction(auction));
    }

    @Secured(ROLE_USER)
    @Transactional
    public void updateInfo(String auctionSeq, Long userId, AuctionUpdateRequest req) {
        Auction auction = auctionRepository
                .findOneBySeqAndSellerId(auctionSeq, userId)
                .orElseThrow(() -> new ClientException(ErrorCode.AUCTION_NOT_FOUND));

        auction.updateInfo(
                req.getStartPrice(),
                req.getBuyNowPrice(),
                req.getBidUnit(),
                req.getIsBuyNowSet(),
                req.getIsAutoExtensible()
        );
    }

    @Secured(ROLE_USER)
    @Transactional
    public void miscarry(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ClientException(ErrorCode.AUCTION_NOT_FOUND));
        auction.misCarry();
    }

    @Secured(ROLE_USER)
    @Transactional
    public void processWinning(String auctionSeq, Long winnerId, Long winPrice) {
        Auction auction = auctionRepository.findOpenOneByAuctionSeq(auctionSeq)
                .orElseThrow(() -> new ClientException(ErrorCode.AUCTION_NOT_FOUND));

        auction.finalize(winnerId, winPrice, auction.getExpiresAt());
    }

    @Transactional
    public AuctionDetail findOneAndIncreaseView(String auctionSeq) {
        Auction findAuction = auctionRepository
                .findOpenOneByAuctionSeq(auctionSeq)
                .orElseThrow(() -> new ClientException(ErrorCode.AUCTION_NOT_FOUND));
        MarketPriceFullResponse marketPrice = marketPriceService.findMarketPriceFullResponse(findAuction.getProduct().getId());

        AuctionDetail detail = AuctionDetail.fromAuction(findAuction, marketPrice);

        // 캐시에 존재하는 경매 정보 업데이트
        Long currentPrice = auctionCacheService.findCurrentPrice(detail.getAuctionSeq());
        Long bidCount = auctionCacheService.findBidCount(detail.getAuctionSeq());
        detail.updateBidInfo(currentPrice, bidCount);

        findAuction.increaseViews();

        return detail;
    }

    @Transactional(readOnly = true)
    public Page<AuctionListItem> findAll(PageCond cond) {
        Pageable pageable = PageRequest.of(cond.getPageNum() - 1, cond.getPageSize());
        Page<AuctionListItem> allOpenByCond = auctionRepository.findAllOpenByCond(pageable);

        // 캐시에 존재하는 경매 정보 업데이트
        allOpenByCond.forEach(a -> {
            Long currentPrice = auctionCacheService.findCurrentPrice(a.getAuctionSeq());
            Long bidCount = auctionCacheService.findBidCount(a.getAuctionSeq());
            a.updateBidInfo(currentPrice, bidCount);
        });

        return allOpenByCond;
    }

    @Secured(ROLE_USER)
    @Transactional(readOnly = true)
    public Page<AuctionLog> findJoinedAuction(PageCond cond, Long userId) {
        Pageable pageable = PageRequest.of(cond.getPageNum() - 1, cond.getPageSize());
        return bidRepository.findJoinedAuction(pageable, userId)
                .map(auc ->
                        switch (auc.getStatus()) {
                            case DONE -> AuctionLog.fromAuction(auc, userId, auc.getWinPrice());
                            case MISCARRY -> AuctionLog.fromAuction(auc, userId, auc.getStartPrice());
                            case OPEN -> AuctionLog.fromAuction(auc, userId,
                                    auctionCacheService.findCurrentPrice(auc.getAuctionSeq()));
                            default -> throw new ClientException(ErrorCode.AUCTION_ACCESS_DENIED);
                        }
                );
    }


}
