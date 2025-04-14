package com.fifteen.auction.domain.recommend.dto.response;

import com.fifteen.auction.domain.recommend.entity.Recommendation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RecommendationResponse {

    private final Long auctionId;
    private final String title;
    private final int score;
    private final int ranking;

    public static RecommendationResponse from(Recommendation recommendation) {
        return new RecommendationResponse(
                recommendation.getAuction().getId(),
                recommendation.getAuction().getProduct().getName(),
                recommendation.getScore(),
                recommendation.getRanking()
        );
    }
}
