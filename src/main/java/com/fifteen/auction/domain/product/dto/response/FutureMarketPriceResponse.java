package com.fifteen.auction.domain.product.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FutureMarketPriceResponse {

    // 향후 3개월 예측 시세
    private List<MarketPriceResponse> predictedPrices;

    // 예측 시세 없음 메시지
    private String predictedPricesMessage;

    public static FutureMarketPriceResponse from(List<MarketPriceResponse> prices, String message) {
        return FutureMarketPriceResponse.builder()
                .predictedPrices(prices)
                .predictedPricesMessage(message)
                .build();
    }
}