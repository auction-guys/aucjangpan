package com.fifteen.auction.domain.auction.service;

import com.fifteen.auction.domain.auction.dto.event.AuctionOpenEvent;
import com.fifteen.auction.domain.auction.dto.request.AuctionCreateRequest;
import com.fifteen.auction.domain.auction.dto.request.AuctionUpdateRequest;
import com.fifteen.auction.domain.auction.dto.response.AuctionDetail;
import com.fifteen.auction.domain.auction.dto.response.AuctionListItem;
import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.domain.auction.entity.AuctionStatus;
import com.fifteen.auction.domain.auction.repository.auction.AuctionRepository;
import com.fifteen.auction.domain.auction.util.AuctionSeqGenerator;
import com.fifteen.auction.domain.product.entity.Product;
import com.fifteen.auction.domain.product.repository.ProductRepository;
import com.fifteen.auction.global.dto.PageCond;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuctionService {
    private final AuctionRepository auctionRepository;
    private final ProductRepository productRepository;
    private final AuctionSeqGenerator auctionSeqGenerator;
    private final AuctionCacheService auctionCacheService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public String create(AuctionCreateRequest req, Long userId) {
        // TODO: 에러코드 변경
        Product product = productRepository.findByIdWithSeller(req.getProductId())
                .orElseThrow(() -> new ClientException(ErrorCode.EXCEPTION));

        // 자신이 생성한 물품인지 확인
        if (!product.getSeller().getId().equals(userId)) {
            throw new ClientException(ErrorCode.NOT_OWNING_PRODUCT);
        }

        String auctionSeq = auctionSeqGenerator.generate(LocalDate.now());

        Auction auction = new Auction(product, auctionSeq,
                req.getStartPrice(), req.getBuyNowPrice(), req.getBidUnit(),
                req.getIsBuyNowSet(), req.getIsAutoExtensible(), req.getExpiresAt());

        Auction savedAuction = auctionRepository.save(auction);

        return savedAuction.getAuctionSeq();
    }


    @Transactional
    public void cancel(String auctionSeq, Long sellerId) {
        Auction auction = auctionRepository
                .findOneBySeqAndSellerId(auctionSeq, sellerId)
                .orElseThrow(() -> new ClientException(ErrorCode.AUCTION_NOT_FOUND));
        auction.cancel(LocalDateTime.now());
    }

    @Transactional
    public void open(String auctionSeq, Long sellerId) {
        Auction auction = auctionRepository
                .findOneBySeqAndSellerId(auctionSeq, sellerId)
                .orElseThrow(() -> new ClientException(ErrorCode.AUCTION_NOT_FOUND));

        if (auction.getStatus() == AuctionStatus.OPEN) {
            throw new ClientException(ErrorCode.AUCTION_ALREADY_OPEN);
        }

        auction.open();
        auctionCacheService.addNewHighPrice(auctionSeq, -1L, auction.getStartPrice());

        // 경매가 개시되면 이벤트 등록 -> 마감 메시지 스케줄링
        applicationEventPublisher.publishEvent(AuctionOpenEvent.fromAuction(auction));
    }

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

        if (auction.getDoneAt() == null) {
            auction.finalize(winnerId, winPrice, auction.getExpiresAt());
        }
    }


    @Transactional(readOnly = true)
    public Page<AuctionListItem> findAll(PageCond cond) {
        Pageable pageable = PageRequest.of(cond.getPageNum() - 1, cond.getPageSize());
        return auctionRepository.findAllOpenByCond(pageable);
    }

    @Transactional(readOnly = true)
    public AuctionDetail findOne(String auctionSeq) {
        Auction findAuction = auctionRepository
                .findOpenOneByAuctionSeq(auctionSeq)
                .orElseThrow(() -> new ClientException(ErrorCode.AUCTION_NOT_FOUND));
        return AuctionDetail.fromAuction(findAuction);
    }

    @Transactional
    public AuctionDetail getAuctionDetailAndIncreaseView(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ClientException(ErrorCode.AUCTION_NOT_FOUND));

        auction.increaseViews();  // views += 1

        return AuctionDetail.fromAuction(auction);
    }

}
