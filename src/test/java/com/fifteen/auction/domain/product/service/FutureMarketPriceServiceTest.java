package com.fifteen.auction.domain.product.service;

import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.domain.auction.entity.AuctionStatus;
import com.fifteen.auction.domain.auction.repository.auction.AuctionRepository;
import com.fifteen.auction.domain.product.dto.response.GPTPricePredictionResponse;
import com.fifteen.auction.domain.product.entity.MarketPrice;
import com.fifteen.auction.domain.product.entity.Product;
import com.fifteen.auction.domain.product.enums.PriceType;
import com.fifteen.auction.domain.product.repository.MarketPriceRepository;
import com.fifteen.auction.domain.product.repository.ProductRepository;
import com.fifteen.auction.global.client.chatgpt.ChatGPTClient;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FutureMarketPriceServiceTest {

    @Mock
    MarketPriceRepository marketPriceRepository;
    @Mock
    ProductRepository productRepository;
    @Mock
    ChatGPTClient chatGPTClient;
    @Mock
    AuctionRepository auctionRepository;

    @InjectMocks
    MarketPriceService marketPriceService;

    @Nested
    class findOrPredictFutureMarketPrices_테스트 {

        @Test
        void 최근상품1개_조회성공_및_예측된가격이_DB에저장된다() throws Exception {
            // given
            String productName = "아이폰15";
            Product product = Product.create(null, null, productName, "256기가");

            Auction auction = new Auction(
                    product, "seq", 10000L, 20000L, 1000,
                    true, false, LocalDateTime.now().plusDays(1)
            );
            Field field = Auction.class.getDeclaredField("winPrice");
            field.setAccessible(true);
            field.set(auction, 1000000L);

            given(productRepository.findTop10ByNameOrderByCreatedAtDesc(productName))
                    .willReturn(List.of(product));
            given(marketPriceRepository.findByProductNameAndPriceDateInAndPriceType(eq(productName), anyList(), eq(PriceType.PREDICTED)))
                    .willReturn(List.of());

            given(auctionRepository.findByProduct_NameAndStatus(eq(productName), eq(AuctionStatus.DONE)))
                    .willReturn(List.of(auction));

            given(chatGPTClient.callGptForFuturePrices(anyString(), anyString(), anyList()))
                    .willReturn(List.of(
                            new GPTPricePredictionResponse("2025-06-01", 900000L, 1100000L),
                            new GPTPricePredictionResponse("2025-07-01", 920000L, 1120000L),
                            new GPTPricePredictionResponse("2025-08-01", 940000L, 1140000L)
                    ));

            // when
            var result = marketPriceService.findOrPredictFutureMarketPrices(productName);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.getPredictedPrices()).hasSize(3);
                softly.assertThat(result.getPredictedPrices().get(0).getMinMarketPrice()).isEqualTo(900000L);
            });
        }

        @Test
        void 최근상품조회결과없으면_빈리스트와메시지를반환한다() {
            // given
            given(productRepository.findTop10ByNameOrderByCreatedAtDesc(anyString()))
                    .willReturn(List.of());

            // when
            var result = marketPriceService.findOrPredictFutureMarketPrices("없는상품");

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.getPredictedPrices()).isEmpty();
                softly.assertThat(result.getPredictedPricesMessage()).isEqualTo("향후 시세 예측 정보가 존재하지 않습니다.");
            });
        }

        @Test
        void DB에_모든가격이있으면_GPT호출하지않는다() {
            // given
            String productName = "갤럭시S24";
            Product product = Product.create(null, null, productName, "미개봉 256기가");

            List<LocalDate> futureDates = List.of(
                    LocalDate.now().plusMonths(1).withDayOfMonth(1),
                    LocalDate.now().plusMonths(2).withDayOfMonth(1),
                    LocalDate.now().plusMonths(3).withDayOfMonth(1)
            );

            List<MarketPrice> existingPrices = futureDates.stream()
                    .map(date -> MarketPrice.builder()
                            .product(product)
                            .priceDate(date)
                            .minMarketPrice(900000L)
                            .maxMarketPrice(1100000L)
                            .priceType(PriceType.PREDICTED)
                            .build())
                    .toList();

            given(productRepository.findTop10ByNameOrderByCreatedAtDesc(productName))
                    .willReturn(List.of(product));
            given(marketPriceRepository.findByProductNameAndPriceDateInAndPriceType(eq(productName), anyList(), eq(PriceType.PREDICTED)))
                    .willReturn(existingPrices);

            // when
            marketPriceService.findOrPredictFutureMarketPrices(productName);

            // then
            verify(chatGPTClient, never()).callGptForFuturePrices(anyString(), anyString(), anyList());
        }
    }
}