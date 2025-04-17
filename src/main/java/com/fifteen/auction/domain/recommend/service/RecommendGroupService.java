package com.fifteen.auction.domain.recommend.service;

import com.fifteen.auction.domain.recommend.entity.RecommendGroup;
import com.fifteen.auction.domain.recommend.enums.AgeGroup;
import com.fifteen.auction.domain.recommend.enums.Gender;
import com.fifteen.auction.domain.recommend.enums.Region;
import com.fifteen.auction.domain.recommend.repository.RecommendGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecommendGroupService {

    private final RecommendGroupRepository recommendGroupRepository;

    public RecommendGroup findOrCreate(Gender gender, AgeGroup ageGroup, Region region) {
        // 조건에 맞는 그룹을 찾습니다.
        Optional<RecommendGroup> existingGroup = recommendGroupRepository.findByGenderAndAgeGroupAndRegion(gender, ageGroup, region);

        // 그룹이 없다면 새로 생성합니다.
        if (existingGroup.isEmpty()) {
            RecommendGroup newGroup = RecommendGroup.create(gender, ageGroup, region);
            return recommendGroupRepository.save(newGroup); // 새 그룹을 저장 후 반환
        }

        // 존재하는 그룹을 반환
        return existingGroup.get();
    }
}
