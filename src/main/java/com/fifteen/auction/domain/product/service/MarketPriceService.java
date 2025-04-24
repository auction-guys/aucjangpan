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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketPriceService {

    private final MarketPriceRepository marketPriceRepository;
    private final ProductRepository productRepository;
    private final ChatGPTClient chatGPTClient;
    private final AuctionRepository auctionRepository;

    @Cacheable(value = "marketPrice", key = "#productName")
    @Transactional
    public MarketPriceFullResponse findMarketPriceFullResponse(String productName) {
        log.info("Redis 캐시 만료! 네이버 API + GPT 호출 & DB 저장! 상품 ID: {}", productName);

        Product product = productRepository.findTopByNameOrderByCreatedAtDesc(productName)
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
        MarketPrice todayPrice;

        GPTPricePredictionResponse dto = predictedPrices.get(predictedPrices.size() - 1);
        boolean alreadySaved = marketPriceRepository.existsByProductIdAndPriceDate(product.getId(), today);

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
                    .findAllByProductIdAndPriceDateBetweenOrderByPriceDateAsc(product.getId(), today, today)
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
                .findAllByProductIdAndPriceDateBetweenOrderByPriceDateAsc(product.getId(), threeMonthsAgo, today.minusDays(1))
                .stream()
                .map(MarketPriceResponse::fromEntity)
                .toList();

        return MarketPriceFullResponse.builder()
                .todayPrice(MarketPriceResponse.fromEntity(todayPrice))
                .historicalPrices(historicalPrices)
                .build();
    }

    // 미래 3개월 시세 예측 기능 (동일 상품이 있는 경우 최근 5개 상품 기준으로 경매 결과 분석)
    @Transactional(readOnly = true)
    public List<GPTPricePredictionResponse> predictFutureMarketPrices(String productName) {
        //최근 등록된 동일 상품명 기준 상품 5개 조회
        List<Product> recentProducts = productRepository.findTop5ByNameOrderByCreatedAtDesc(productName);
        if (recentProducts.isEmpty()) {
            throw new ServerException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        //description 5개를 모두 합쳐서 프롬프트 생성
        String combinedDescription = recentProducts.stream()
                .map(Product::getDescription)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.joining("\n"));

        // 경매에서 DONE 상태 낙찰가 수집
        List<Long> winPrices = recentProducts.stream()
                .flatMap(product -> auctionRepository.findByProduct_NameAndStatus(product.getName(), AuctionStatus.DONE).stream())
                .map(Auction::getWinPrice)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (winPrices.isEmpty()) {
            throw new ServerException(ErrorCode.MARKET_PRICE_NOT_FOUND);
        }

        return chatGPTClient.callGptForFuturePrices(productName, combinedDescription, winPrices);
    }
}



