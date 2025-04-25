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
public class MarketPriceFullResponse {
    private MarketPriceResponse todayPrice; // 오늘 시세 (Redis 캐시)
    private List<MarketPriceResponse> historicalPrices; // 최근 3개월 시세 (DB)
    private String todayPriceMessage; // 오늘 시세 정보 없을 때 출력 메시지
    private String historicalPricesMessage; // 최근 3개월 시세 정보 없을 때 출력 메시지
}
