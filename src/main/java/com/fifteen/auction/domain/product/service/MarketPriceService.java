package com.fifteen.auction.domain.product.service;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketPriceService {

    private final MarketPriceRepository marketPriceRepository;
    private final ProductRepository productRepository;
    private final ChatGPTClient openAIClient;

    @Cacheable(value = "marketPrice", key = "#productId")
    @Transactional
    public MarketPriceFullResponse findMarketPriceFullResponse(Long productId) {
        log.info("Redis 캐시 만료! 네이버 API + GPT 호출 & DB 저장! 상품 ID: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ServerException(ErrorCode.PRODUCT_NOT_FOUND));

        // 네이버 API + GPT 호출을 통해 오늘 포함 최근 3개월 시세 예측
        List<GPTPricePredictionResponse> predictedPrices = openAIClient.callGptForHistoricalPrices(
                product.getName(),
                product.getDescription()
        );

        if (predictedPrices.isEmpty()) {
            throw new ServerException(ErrorCode.MARKET_PRICE_NOT_FOUND);
        }

        Set<LocalDate> savedDates = new HashSet<>();
        MarketPrice todayPrice = null;

        for (int i = 0; i < predictedPrices.size(); i++) {
            GPTPricePredictionResponse dto = predictedPrices.get(i);
            LocalDate priceDate = LocalDate.parse(dto.getDate());

            // 중복 날짜 필터링
            if (!savedDates.add(priceDate)) continue;

            boolean isToday = (i == predictedPrices.size() - 1);
            boolean alreadySaved = marketPriceRepository.existsByProductIdAndPriceDate(productId, priceDate);

            // 이미 저장된 과거 데이터는 스킵
            if (!isToday && alreadySaved) continue;

            MarketPrice price = MarketPrice.builder()
                    .product(product)
                    .priceDate(priceDate)
                    .minMarketPrice(dto.getMin())
                    .maxMarketPrice(dto.getMax())
                    .build();

            if (!isToday) {
                // 과거 데이터만 DB 저장
                marketPriceRepository.save(price);
            } else {
                // 오늘 데이터는 변수에만 저장하고, DB 저장 안 함
                todayPrice = price;
            }
        }

        if (todayPrice == null) {
            throw new ServerException(ErrorCode.MARKET_PRICE_NOT_FOUND);
        }

        // DB에 저장된 최근 3개월 시세 조회 및 DTO 반환
        List<MarketPriceResponse> historicalPrices = marketPriceRepository
                .findAllByProductIdOrderByPriceDateAsc(productId)
                .stream()
                .map(MarketPriceResponse::fromEntity)
                .toList();

        return MarketPriceFullResponse.builder()
                .todayPrice(MarketPriceResponse.fromEntity(todayPrice))
                .historicalPrices(historicalPrices)
                .build();
    }
}
