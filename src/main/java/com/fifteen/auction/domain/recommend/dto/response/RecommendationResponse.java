package com.fifteen.auction.domain.recommend.dto.response;

import com.fifteen.auction.domain.auction.entity.Auction;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RecommendationResponse {
    private final Long auctionId;
    private final String productName;
    private final String thumbnailUrl;
    private final int score;
    private final int ranking;

    public static RecommendationResponse of(Auction auction, int score, int ranking) {
        return new RecommendationResponse(
                auction.getId(),
                auction.getProduct().getName(),
                auction.getProduct().getThumbnailUrl(),
                score,
                ranking
        );
    }
}
