package com.fifteen.auction.domain.product.service;

import com.fifteen.auction.domain.product.dto.response.GPTPricePredictionResponse;
import com.fifteen.auction.domain.product.dto.response.MarketPriceFullResponse;
import com.fifteen.auction.domain.product.entity.Product;
import com.fifteen.auction.domain.product.enums.PriceType;
import com.fifteen.auction.domain.product.repository.MarketPriceRepository;
import com.fifteen.auction.domain.product.repository.ProductRepository;
import com.fifteen.auction.global.client.chatgpt.ChatGPTClient;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ServerException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MarketPriceServiceTest {

    @Mock
    MarketPriceRepository marketPriceRepository;
    @Mock
    ProductRepository productRepository;
    @Mock
    ChatGPTClient chatGPTClient;

    @InjectMocks
    MarketPriceService marketPriceService;

    @Nested
    class findMarketPriceFullResponse_테스트 {

        @Test
        void 성공적으로_오늘시세와_최근3개월시세를_조회한다() {
            // given
            String productName = "아이폰15";
            Product product = Product.create(null, null, productName, "256기가");
            ReflectionTestUtils.setField(product, "id", 1L);
            LocalDate today = LocalDate.now();
            GPTPricePredictionResponse todayPriceDto = new GPTPricePredictionResponse(today.toString(), 1000000L, 1200000L);

            given(productRepository.findTopByNameOrderByCreatedAtDesc(productName)).willReturn(Optional.of(product));
            given(chatGPTClient.callGptForHistoricalPrices(anyString(), anyString())).willReturn(List.of(todayPriceDto));
            given(marketPriceRepository.existsByProductIdAndPriceDateAndPriceType(anyLong(), eq(today), eq(PriceType.ACTUAL)))
                    .willReturn(false);

            // when
            MarketPriceFullResponse response = marketPriceService.findMarketPriceFullResponse(productName);

            // then
            assertSoftly(softly -> {
                softly.assertThat(response.getTodayPrice()).isNotNull();
                softly.assertThat(response.getTodayPrice().getMinMarketPrice()).isEqualTo(1000000L);
                softly.assertThat(response.getTodayPrice().getMaxMarketPrice()).isEqualTo(1200000L);
                softly.assertThat(response.getHistoricalPrices()).isEmpty();
            });
        }

        @Test
        void 상품이_존재하지_않으면_예외가_발생한다() {
            // given
            given(productRepository.findTopByNameOrderByCreatedAtDesc(anyString()))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> marketPriceService.findMarketPriceFullResponse("없는상품"))
                    .isInstanceOf(ServerException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
        }

        @Test
        void GPT호출에_실패하면_오늘시세는_null로_반환된다() {
            // given
            Product product = Product.create(null, null, "아이폰15", "256기가");

            given(productRepository.findTopByNameOrderByCreatedAtDesc(anyString()))
                    .willReturn(Optional.of(product));
            given(chatGPTClient.callGptForHistoricalPrices(anyString(), anyString()))
                    .willReturn(List.of());

            // when
            MarketPriceFullResponse response = marketPriceService.findMarketPriceFullResponse("아이폰15");

            // then
            assertSoftly(softly -> {
                softly.assertThat(response.getTodayPrice()).isNull();
                softly.assertThat(response.getTodayPriceMessage()).isEqualTo("오늘 시세 정보가 존재하지 않습니다.");
            });
        }
    }
}