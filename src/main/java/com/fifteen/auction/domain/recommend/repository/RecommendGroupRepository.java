package com.fifteen.auction.domain.recommend.repository;

import com.fifteen.auction.domain.recommend.entity.RecommendGroup;
import com.fifteen.auction.domain.recommend.enums.AgeGroup;
import com.fifteen.auction.domain.recommend.enums.Gender;
import com.fifteen.auction.domain.recommend.enums.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecommendGroupRepository extends JpaRepository<RecommendGroup, Long> {

    Optional<RecommendGroup> findByGenderAndAgeGroupAndRegion(Gender gender, AgeGroup ageGroup, Region region);
}
