package com.fifteen.auction.domain.product.service;

import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.domain.auction.entity.AuctionStatus;
import com.fifteen.auction.domain.auction.repository.auction.AuctionRepository;
import com.fifteen.auction.domain.product.dto.response.GPTPricePredictionResponse;
import com.fifteen.auction.domain.product.dto.response.MarketPriceFullResponse;
import com.fifteen.auction.domain.product.dto.response.MarketPriceResponse;
import com.fifteen.auction.domain.product.entity.MarketPrice;
import com.fifteen.auction.domain.product.entity.Product;
import com.fifteen.auction.domain.product.repository.MarketPriceRepository;
import com.fifteen.auction.domain.product.repository.ProductRepository;
import com.fifteen.auction.global.client.chatgpt.ChatGPTClient;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketPriceService {

    private final MarketPriceRepository marketPriceRepository;
    private final ProductRepository productRepository;
    private final ChatGPTClient chatGPTClient;
    private final AuctionRepository auctionRepository;

    @Cacheable(value = "marketPrice", key = "#productId")
    @Transactional
    public MarketPriceFullResponse findMarketPriceFullResponse(Long productId) {
        log.info("Redis 캐시 만료! 네이버 API + GPT 호출 & DB 저장! 상품 ID: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ServerException(ErrorCode.PRODUCT_NOT_FOUND));

        // 네이버 API + GPT 호출을 통해 오늘 시세 예측
        List<GPTPricePredictionResponse> predictedPrices = chatGPTClient.callGptForHistoricalPrices(
                product.getName(),
                product.getDescription()
        );

        if (predictedPrices.isEmpty()) {
            throw new ServerException(ErrorCode.MARKET_PRICE_NOT_FOUND);
        }

        LocalDate today = LocalDate.now();
        MarketPrice todayPrice = null;

        GPTPricePredictionResponse dto = predictedPrices.get(predictedPrices.size() - 1);
        boolean alreadySaved = marketPriceRepository.existsByProductIdAndPriceDate(productId, today);

        if (!alreadySaved) {
            todayPrice = MarketPrice.builder()
                    .product(product)
                    .priceDate(today)
                    .minMarketPrice(dto.getMin())
                    .maxMarketPrice(dto.getMax())
                    .build();

            marketPriceRepository.save(todayPrice);
        } else {
            todayPrice = marketPriceRepository
                    .findAllByProductIdAndPriceDateBetweenOrderByPriceDateAsc(productId, today, today)
                    .stream()
                    .findFirst()
                    .map(MarketPriceResponse::fromEntity)
                    .map(mp -> MarketPrice.builder()
                            .product(product)
                            .priceDate(today)
                            .minMarketPrice(mp.getMinMarketPrice())
                            .maxMarketPrice(mp.getMaxMarketPrice())
                            .build())
                    .orElseThrow(() -> new ServerException(ErrorCode.MARKET_PRICE_NOT_FOUND));
        }

        LocalDate threeMonthsAgo = today.minusMonths(3).withDayOfMonth(1);

        // DB에 저장된 최근 3개월 시세 조회 및 DTO 반환
        List<MarketPriceResponse> historicalPrices = marketPriceRepository
                .findAllByProductIdAndPriceDateBetweenOrderByPriceDateAsc(productId, threeMonthsAgo, today.minusDays(1))
                .stream()
                .map(MarketPriceResponse::fromEntity)
                .toList();

        return MarketPriceFullResponse.builder()
                .todayPrice(MarketPriceResponse.fromEntity(todayPrice))
                .historicalPrices(historicalPrices)
                .build();
    }

    // 미래 3개월 시세 예측 기능
    @Transactional(readOnly = true)
    public List<GPTPricePredictionResponse> predictFutureMarketPrices(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ServerException(ErrorCode.PRODUCT_NOT_FOUND));

        List<Auction> completedAuctions = auctionRepository.findByProduct_IdAndStatus(productId, AuctionStatus.DONE);

        List<Long> winPrices = completedAuctions.stream()
                .map(Auction::getWinPrice)
                .filter(Objects::nonNull)
                .toList();

        if (winPrices.isEmpty()) {
            throw new ServerException(ErrorCode.MARKET_PRICE_NOT_FOUND);
        }

        return chatGPTClient.callGptForFuturePrices(
                product.getName(),
                product.getDescription(),
                winPrices
        );
    }
}


