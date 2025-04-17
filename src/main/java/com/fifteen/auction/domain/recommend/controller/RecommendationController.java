package com.fifteen.auction.domain.recommend.controller;

import com.fifteen.auction.domain.recommend.dto.response.RecommendationResponse;
import com.fifteen.auction.domain.recommend.service.RecommendService;
import com.fifteen.auction.domain.recommend.entity.RecommendGroup;
import com.fifteen.auction.domain.recommend.repository.RecommendGroupRepository;
import com.fifteen.auction.global.dto.Response;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {

    private final RecommendService recommendService;
    private final RecommendGroupRepository recommendGroupRepository;

    @PostMapping("/{groupId}")
    public ResponseEntity<Void> generateRecommendations(@PathVariable Long groupId) {
        RecommendGroup group = getGroup(groupId);
        recommendService.generateRecommendationsForGroup(group);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<Void> refreshRecommendations(@PathVariable Long groupId) {
        RecommendGroup group = getGroup(groupId);
        recommendService.generateRecommendationsForGroup(group);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<Response<List<RecommendationResponse>>> getRecommendations(@PathVariable Long groupId) {
        RecommendGroup group = getGroup(groupId);
        List<RecommendationResponse> result = recommendService.getRecommendationsForGroup(group);
        return ResponseEntity.ok(Response.of(result));
    }

    private RecommendGroup getGroup(Long groupId) {
        return recommendGroupRepository.findById(groupId)
                .orElseThrow(() -> new ClientException(ErrorCode.RECOMMEND_NOT_FOUND));
    }
}