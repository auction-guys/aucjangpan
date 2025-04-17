package com.fifteen.auction.domain.recommend.entity;

import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "recommendation")
public class Recommendation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommend_group_id", nullable = false)
    private RecommendGroup recommendGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private int ranking;

    public static Recommendation create(RecommendGroup recommendGroup, Auction auction, int score, int ranking) {
        Recommendation recommendation = new Recommendation();
        recommendation.recommendGroup = recommendGroup;
        recommendation.auction = auction;
        recommendation.score = score;
        recommendation.ranking = ranking;
        return recommendation;
    }
}
