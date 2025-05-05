package com.fifteen.auction.domain.auction.service;

import com.fifteen.auction.domain.auction.dto.event.AuctionOpenEvent;
import com.fifteen.auction.domain.auction.dto.request.AuctionCreateRequest;
import com.fifteen.auction.domain.auction.dto.request.AuctionUpdateRequest;
import com.fifteen.auction.domain.auction.dto.response.*;
import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.domain.auction.repository.auction.AuctionRedisRepository;
import com.fifteen.auction.domain.auction.repository.auction.AuctionRepository;
import com.fifteen.auction.domain.auction.repository.bid.BidRepository;
import com.fifteen.auction.domain.auction.service.port.out.AuctionEndScheduleService;
import com.fifteen.auction.domain.auction.util.AuctionSeqGenerator;
import com.fifteen.auction.domain.auction.util.ClockHolder;
import com.fifteen.auction.domain.product.dto.response.FutureMarketPriceResponse;
import com.fifteen.auction.domain.product.dto.response.MarketPriceFullResponse;
import com.fifteen.auction.domain.product.entity.Product;
import com.fifteen.auction.domain.product.repository.ProductRepository;
import com.fifteen.auction.domain.product.service.MarketPriceService;
import com.fifteen.auction.domain.recommend.repository.RecommendRedisRepository;
import com.fifteen.auction.domain.recommend.service.RedisService;
import com.fifteen.auction.domain.tag.entity.Tag;
import com.fifteen.auction.domain.tag.repository.TagRepository;
import com.fifteen.auction.domain.user.repository.UserRepository;
import com.fifteen.auction.global.dto.PageCond;
import com.fifteen.auction.global.dto.PageInfo;
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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static com.fifteen.auction.domain.user.enums.UserRole.Authority.ROLE_USER;

@Service
@RequiredArgsConstructor
public class AuctionService {
    private final AuctionRepository auctionRepository;
    private final ProductRepository productRepository;
    private final BidRepository bidRepository;

    private final MarketPriceService marketPriceService;
    private final AuctionRedisRepository auctionRedisRepository;

    private final AuctionSeqGenerator auctionSeqGenerator;
    private final ClockHolder clockHolder;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final AuctionEndScheduleService auctionEndScheduleService;

    private final RedisService redisService;
    private final TagRepository tagRepository;

    private final UserRepository userRepository;
    private final RecommendRedisRepository recommendRedisRepository;

    @Secured(ROLE_USER)
    @Transactional
    public String create(AuctionCreateRequest req, Long userId) {
        Product product = productRepository.findByIdWithSeller(req.getProductId())
                .orElseThrow(() -> new ClientException(ErrorCode.PRODUCT_NOT_FOUND));

        if (!product.isUserASeller(userId)) {
            throw new ClientException(ErrorCode.NOT_OWNING_PRODUCT);
        }

        String auctionSeq = auctionSeqGenerator.generate(LocalDate.now());

        Auction auction = new Auction(product, auctionSeq,
                req.getStartPrice(), req.getBuyNowPrice(), req.getBidUnit(),
                req.getIsBuyNowSet(), req.getIsAutoExtensible(), req.getExpiresAt());

        // 태그 연동
        if (req.getTagIds() != null && !req.getTagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(req.getTagIds());
            auction.addTags(tags); // Auction 내부에서 AuctionTag로 매핑
        }

        return auctionRepository.save(auction).getAuctionSeq();
    }

    @Secured(ROLE_USER)
    @Transactional
    public void cancel(String auctionSeq, Long sellerId) {
        Auction auction = auctionRepository
                .findOneBySeqAndSellerId(auctionSeq, sellerId)
                .orElseThrow(() -> new ClientException(ErrorCode.AUCTION_NOT_FOUND));
        auction.cancel(clockHolder.now());
    }

    @Secured(ROLE_USER)
    @Transactional
    public void open(String auctionSeq, Long sellerId) {
        Auction auction = auctionRepository
                .findOneBySeqAndSellerId(auctionSeq, sellerId)
                .orElseThrow(() -> new ClientException(ErrorCode.AUCTION_NOT_FOUND));

        LocalDateTime now = clockHolder.now();

        auction.open(now);
        auctionRedisRepository.initializeAuction(auctionSeq, auction.getStartPrice());

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
                req.getIsAutoExtensible(),
                req.getExpiresAt()
        );
    }

    @Transactional
    public void miscarry(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ClientException(ErrorCode.AUCTION_NOT_FOUND));
        auction.misCarry();
    }

    @Transactional
    public void processWinning(String auctionSeq, Long winnerId, Long winPrice) {
        Auction auction = auctionRepository.findOpenOneByAuctionSeq(auctionSeq)
                .orElseThrow(() -> new ClientException(ErrorCode.AUCTION_NOT_FOUND));

        auction.finalize(winnerId, winPrice, auction.getExpiresAt());
    }

    @Transactional
    public AuctionDetail findOneAndIncreaseView(String auctionSeq, String userKey) {
        Auction findAuction = auctionRepository
                .findOpenOneByAuctionSeq(auctionSeq)
                .orElseThrow(() -> new ClientException(ErrorCode.AUCTION_NOT_FOUND));

        MarketPriceFullResponse marketPrice = marketPriceService.findMarketPriceFullResponse(findAuction.getProduct().getName());

        FutureMarketPriceResponse futurePrices = marketPriceService.findOrPredictFutureMarketPrices(
                findAuction.getProduct().getName()
        );

        AuctionDetail detail = AuctionDetail.fromAuction(findAuction, marketPrice, futurePrices);

        // 캐시에 존재하는 경매 정보 업데이트
        Long currentPrice = auctionRedisRepository.findCurrentPrice(detail.getAuctionSeq());
        Long bidCount = auctionRedisRepository.findBidCount(detail.getAuctionSeq());
        detail.updateBidInfo(currentPrice, bidCount);

        // 조회수 증가: Redis에 key가 없을 때만 증가
        String redisKey = "view:auction:" + findAuction.getId() + ":user:" + userKey;
        Duration ttl = Duration.ofMinutes(60); // 중복 방지 시간
//        Duration ttl = Duration.ofHours(1); // 1시간에 1번만 view 증가

        if (!redisService.isViewedRecently(redisKey)) {
            findAuction.increaseViews();
            redisService.markViewed(redisKey, ttl);
        }

        return detail;
    }

    @Transactional(readOnly = true)
    public Page<AuctionListItem> findAll(PageCond cond) {
        Pageable pageable = PageRequest.of(cond.getPageNum() - 1, cond.getPageSize());
        Page<AuctionListItem> allOpenByCond = auctionRepository.findAllOpenByCond(pageable);

        // 캐시에 존재하는 경매 정보 업데이트
        allOpenByCond.forEach(a -> {
            Long currentPrice = auctionRedisRepository.findCurrentPrice(a.getAuctionSeq());
            Long bidCount = auctionRedisRepository.findBidCount(a.getAuctionSeq());
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
                                    auctionRedisRepository.findCurrentPrice(auc.getAuctionSeq()));
                            default -> throw new ClientException(ErrorCode.AUCTION_ACCESS_DENIED);
                        }
                );
    }


    /**
     * V2 services
     */
    @Secured(ROLE_USER)
    @Transactional
    public void openV2(String auctionSeq, Long sellerId) {
        Auction auction = auctionRepository
                .findOneBySeqAndSellerId(auctionSeq, sellerId)
                .orElseThrow(() -> new ClientException(ErrorCode.AUCTION_NOT_FOUND));

        LocalDateTime now = clockHolder.now();

        auction.open(now);
        auctionRedisRepository.initializeAuction(auctionSeq, auction.getStartPrice());

        // 경매가 개시되면 이벤트 등록 -> 마감 메시지 스케줄링
        auctionEndScheduleService.scheduleAuctionEnd(AuctionOpenEvent.fromAuction(auction));
    }

    @Transactional
    public CurrentPriceInfo findCurrentPrice(String auctionSeq) {
        Long currentPrice = auctionRedisRepository.findCurrentPrice(auctionSeq);
        return new CurrentPriceInfo(currentPrice);
    }

    @Secured(ROLE_USER)
    @Transactional(readOnly = true)
    public AuctionOverviewResponse getAuctionOverview(Long userId, PageCond cond) {
        // 1. 추천 그룹 ID 조회
        Long groupId = userRepository.findRecommendGroupId(userId)
                .orElseThrow(() -> new ClientException(ErrorCode.RECOMMEND_NOT_FOUND));

        // 2. Redis에서 추천 ID 조회
        Set<String> redisIds = recommendRedisRepository.findTopAuctionIds(groupId, 10);
        List<Long> recommendedIds = redisIds.stream().map(Long::valueOf).toList();

        // 3. 추천 옥션 상세 조회 (순서 유지)
        List<AuctionListItem> recommended = auctionRepository.findListItemsByIds(recommendedIds);
        recommended.forEach(a -> {
            Long currentPrice = auctionRedisRepository.findCurrentPrice(a.getAuctionSeq());
            Long bidCount = auctionRedisRepository.findBidCount(a.getAuctionSeq());
            a.updateBidInfo(currentPrice, bidCount);
        });

        // 4. 나머지 옥션 페이징 조회
        Pageable pageable = PageRequest.of(cond.getPageNum() - 1, cond.getPageSize());
        Page<AuctionListItem> others = auctionRepository.findAllOpenExcludingIds(recommendedIds, pageable);
        others.forEach(a -> {
            Long currentPrice = auctionRedisRepository.findCurrentPrice(a.getAuctionSeq());
            Long bidCount = auctionRedisRepository.findBidCount(a.getAuctionSeq());
            a.updateBidInfo(currentPrice, bidCount);
        });

        return new AuctionOverviewResponse(recommended, others.getContent(), PageInfo.fromPage(others));
    }
}

