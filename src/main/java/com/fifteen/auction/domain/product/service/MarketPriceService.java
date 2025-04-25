package com.fifteen.auction.domain.product.service;

import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.domain.auction.entity.AuctionStatus;
import com.fifteen.auction.domain.auction.repository.auction.AuctionRepository;
import com.fifteen.auction.domain.product.dto.response.FutureMarketPriceResponse;
import com.fifteen.auction.domain.product.dto.response.GPTPricePredictionResponse;
import com.fifteen.auction.domain.product.dto.response.MarketPriceFullResponse;
import com.fifteen.auction.domain.product.dto.response.MarketPriceResponse;
import com.fifteen.auction.domain.product.entity.MarketPrice;
import com.fifteen.auction.domain.product.entity.Product;
import com.fifteen.auction.domain.product.enums.PriceType;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

        LocalDate today = LocalDate.now();
        MarketPrice todayPrice = null;
        String todayPriceMessage = null;

        // 네이버 API + GPT 호출을 통해 오늘 시세 예측
        List<GPTPricePredictionResponse> predictedPrices = null;
        try {
            predictedPrices = chatGPTClient.callGptForHistoricalPrices(
                    product.getName(),
                    product.getDescription()
            );
        } catch (ServerException e) {
            log.warn("오늘 시세 GPT 호출 실패: {}", e.getMessage());
            todayPriceMessage = "오늘 시세 정보가 존재하지 않습니다.";
        }

        // 예측 가격 존재 시 저장 or 조회
        if (predictedPrices != null && !predictedPrices.isEmpty()) {
            GPTPricePredictionResponse dto = predictedPrices.get(predictedPrices.size() - 1);
            boolean alreadySaved = marketPriceRepository.existsByProductIdAndPriceDateAndPriceType(product.getId(), today, PriceType.ACTUAL);

            if (!alreadySaved) {
                todayPrice = MarketPrice.builder()
                        .product(product)
                        .priceDate(today)
                        .minMarketPrice(dto.getMin())
                        .maxMarketPrice(dto.getMax())
                        .priceType(PriceType.ACTUAL)
                        .build();

                marketPriceRepository.save(todayPrice);
            } else {
                todayPrice = marketPriceRepository
                        .findFirstByProductIdAndPriceDateAndPriceType(product.getId(), today, PriceType.ACTUAL)
                        .orElse(null);
            }
        } else {
            todayPriceMessage = "오늘 시세 정보가 존재하지 않습니다.";
        }

        // DB에 저장된 최근 3개월 시세 조회 및 DTO 반환
        List<MarketPriceResponse> historicalPrices = new ArrayList<>();
        String historicalPricesMessage = null;

        for (int i = 3; i >= 1; i--) {
            LocalDate targetDate = today.minusMonths(i).withDayOfMonth(1);

            List<MarketPrice> prices = marketPriceRepository.findByProductNameAndPriceDateAndPriceType(productName, targetDate, PriceType.ACTUAL);
            prices.stream()
                    .sorted(Comparator.comparing(mp -> mp.getProduct().getCreatedAt(), Comparator.reverseOrder()))
                    .findFirst()
                    .ifPresent(mp -> historicalPrices.add(MarketPriceResponse.fromEntity(mp)));

            if (prices.isEmpty()) {
                log.info("{} 기준의 시세 정보를 찾을 수 없습니다.", targetDate);
            }
        }

        if (historicalPrices.isEmpty()) {
            historicalPricesMessage = " 1일 기준 최근 3개월 시세 정보가 존재하지 않습니다.";
        }

        return MarketPriceFullResponse.builder()
                .todayPriceMessage(todayPriceMessage)
                .todayPrice(todayPrice != null ? MarketPriceResponse.fromEntity(todayPrice) : null)
                .historicalPrices(historicalPrices)
                .historicalPricesMessage(historicalPricesMessage)
                .build();
    }


    // 미래 3개월 시세 예측 기능 (동일 상품이 있는 경우 최근 5개 상품 기준으로 경매 결과 분석)
    @Transactional
    public FutureMarketPriceResponse findOrPredictFutureMarketPrices(String productName) {
        LocalDate today = LocalDate.now();
        List<LocalDate> futureDates = IntStream.rangeClosed(1, 3)
                .mapToObj(i -> today.plusMonths(i).withDayOfMonth(1))
                .toList();
        //최근 등록된 동일 상품명 기준 상품 5개 조회
        List<Product> recentProducts = productRepository.findTop10ByNameOrderByCreatedAtDesc(productName);
        if (recentProducts.isEmpty()) {
            return FutureMarketPriceResponse.builder()
                    .prices(List.of())
                    .message("향후 시세 예측 정보가 존재하지 않습니다.")
                    .build();
        }

        Product productRef = recentProducts.get(0);

        List<MarketPrice> existingPrices = marketPriceRepository
                .findByProductNameAndPriceDateInAndPriceType(productName, futureDates, PriceType.PREDICTED);

        Map<LocalDate, MarketPrice> existingPriceMap = existingPrices.stream()
                .collect(Collectors.toMap(MarketPrice::getPriceDate, mp -> mp));

        List<LocalDate> missingDates = futureDates.stream()
                .filter(date -> !existingPriceMap.containsKey(date))
                .toList();

        List<GPTPricePredictionResponse> predicted = new ArrayList<>();

        if (!missingDates.isEmpty()) {
            List<Long> winPrices = recentProducts.stream()
                    .flatMap(product -> auctionRepository.findByProduct_NameAndStatus(product.getName(), AuctionStatus.DONE).stream())
                    .map(Auction::getWinPrice)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        //description 5개를 모두 합쳐서 프롬프트 생성
        String combinedDescription = recentProducts.stream()
                .map(Product::getDescription)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.joining("\n"));

            predicted = chatGPTClient.callGptForFuturePrices(productName, combinedDescription, winPrices);

            List<MarketPrice> toSave = predicted.stream()
                    .filter(p -> missingDates.contains(LocalDate.parse(p.getDate())))
                    .map(p -> MarketPrice.builder()
                            .product(productRef)
                            .priceDate(LocalDate.parse(p.getDate()))
                            .minMarketPrice(p.getMin())
                            .maxMarketPrice(p.getMax())
                            .priceType(PriceType.PREDICTED)
                            .build())
                    .toList();

            marketPriceRepository.saveAll(toSave);
            toSave.forEach(mp -> existingPriceMap.put(mp.getPriceDate(), mp));
        }

        List<MarketPriceResponse> finalResult = futureDates.stream()
                .map(existingPriceMap::get)
                .filter(Objects::nonNull)
                .map(MarketPriceResponse::fromEntity)
                .toList();

        return FutureMarketPriceResponse.builder()
                .prices(finalResult)
                .build();
    }
}
