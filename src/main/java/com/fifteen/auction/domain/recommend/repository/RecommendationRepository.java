package com.fifteen.auction.domain.recommend.repository;

import com.fifteen.auction.domain.recommend.entity.RecommendGroup;
import com.fifteen.auction.domain.recommend.entity.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    List<Recommendation> findAllByRecommendGroupOrderByRankingAsc(RecommendGroup recommendGroup);

    void deleteAllByRecommendGroup(RecommendGroup recommendGroup);

    boolean existsByAuction_Id(Long auctionId);
}
