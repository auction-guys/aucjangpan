package com.fifteen.auction.domain.recommend.scheduler;

import com.fifteen.auction.domain.recommend.entity.RecommendGroup;
import com.fifteen.auction.domain.recommend.repository.RecommendGroupRepository;
import com.fifteen.auction.domain.recommend.service.RecommendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendScheduler {

    private final RecommendGroupRepository groupRepository;
    private final RecommendService recommendService;

    // 매시간 정각마다 실행
    @Scheduled(cron = "0 0 * * * *") // 초 분 시 일 월 요일
    public void updateAllRecommendations() {
        List<RecommendGroup> groups = groupRepository.findAll();

        log.info("[배치 시작] 전체 그룹 수: {}", groups.size());
        for (RecommendGroup group : groups) {
            recommendService.generateRecommendationsForGroup(group); // 기존 추천 생성 로직 재사용
            log.info("추천 갱신 완료 - groupId: {}", group.getId());
        }
        log.info("[배치 종료]");
    }
}
