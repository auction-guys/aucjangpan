package com.fifteen.auction.domain.recommend.repository;

import com.fifteen.auction.domain.recommend.entity.Recommend;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendRepository extends JpaRepository<Recommend, Long> {

    List<Recommend> findTop10ByGroupIdOrderByRankAsc(Long groupId);
}
