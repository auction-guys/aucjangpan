package com.fifteen.auction.domain.product.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FutureMarketPriceResponse {
    private List<MarketPriceResponse> prices;
    private String message;


    public static FutureMarketPriceResponse fromGPT(Long productId, List<GPTPricePredictionResponse> gptPrices) {
        if (gptPrices == null || gptPrices.isEmpty()) {
            return FutureMarketPriceResponse.builder()
                    .prices(List.of())
                    .message("추후 시세 예측 정보가 존재하지 않습니다.")
                    .build();
        }

        List<MarketPriceResponse> mapped = gptPrices.stream()
                .map(p -> new MarketPriceResponse(
                        productId,
                        p.getMin(),
                        p.getMax(),
                        LocalDate.parse(p.getDate())
                ))
                .toList();

        return FutureMarketPriceResponse.builder()
                .prices(mapped)
                .build();
    }
}
