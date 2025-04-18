package com.fifteen.auction.domain.recommend.controller;

import com.fifteen.auction.domain.recommend.dto.response.RecommendationResponse;
import com.fifteen.auction.domain.recommend.entity.RecommendGroup;
import com.fifteen.auction.domain.recommend.service.RecommendService;
import com.fifteen.auction.global.dto.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {

    private final RecommendService recommendService;

    @PostMapping("/{groupId}")
    public ResponseEntity<Void> generateRecommendations(@PathVariable Long groupId) {
        RecommendGroup group = recommendService.findGroup(groupId); // ✅ 서비스에서 가져오기
        recommendService.generateRecommendationsForGroup(group);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<Void> refreshRecommendations(@PathVariable Long groupId) {
        RecommendGroup group = recommendService.findGroup(groupId); // ✅ 중복 제거
        recommendService.generateRecommendationsForGroup(group);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<Response<List<RecommendationResponse>>> getRecommendations(@PathVariable Long groupId) {
        RecommendGroup group = recommendService.findGroup(groupId); // ✅ 동일하게 호출
        List<RecommendationResponse> result = recommendService.getRecommendationsForGroup(group);
        return ResponseEntity.ok(Response.of(result));
    }
}