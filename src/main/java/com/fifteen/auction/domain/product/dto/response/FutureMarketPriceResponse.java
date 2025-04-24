package com.fifteen.auction.domain.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FutureMarketPriceResponse {
    private List<MarketPriceResponse> prices;

    public static FutureMarketPriceResponse fromGPT(Long productId, List<GPTPricePredictionResponse> gptPrices) {
        List<MarketPriceResponse> mapped = gptPrices.stream()
                .map(p -> new MarketPriceResponse(
                        productId,
                        p.getMin(),
                        p.getMax(),
                        LocalDate.parse(p.getDate())
                ))
                .toList();
        return new FutureMarketPriceResponse(mapped);
    }
}
