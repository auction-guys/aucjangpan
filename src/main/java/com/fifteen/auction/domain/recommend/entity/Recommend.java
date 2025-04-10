package com.fifteen.auction.domain.recommend.entity;

import com.fifteen.auction.domain.auction.entity.Auction;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "group_recommend_auction")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recommend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long groupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    private Double score;

    @Column(name = "ranking")
    private Integer rank;

    private Recommend(Long groupId, Auction auction, Double score, Integer rank) {
        this.groupId = groupId;
        this.auction = auction;
        this.score = score;
        this.rank = rank;
    }

    public static Recommend create(Long groupId, Auction auction, Double score, Integer rank) {
        return new Recommend(groupId, auction, score, rank);
    }
}