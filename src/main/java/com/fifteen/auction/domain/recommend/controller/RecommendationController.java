package com.fifteen.auction.domain.recommend.controller;

import com.fifteen.auction.domain.recommend.dto.response.RecommendationResponse;
import com.fifteen.auction.domain.recommend.entity.RecommendGroup;
import com.fifteen.auction.domain.recommend.service.RecommendService;
import com.fifteen.auction.domain.user.auth.entity.AuthUser;
import com.fifteen.auction.global.dto.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {

    private final RecommendService recommendService;

    @PostMapping("/{groupId}")
    public ResponseEntity<Void> generateRecommendations(
            @PathVariable Long groupId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        RecommendGroup group = recommendService.findGroup(groupId);
        recommendService.generateRecommendationsForGroup(group, authUser.getId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<Void> refreshRecommendations(
            @PathVariable Long groupId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        RecommendGroup group = recommendService.findGroup(groupId);
        recommendService.generateRecommendationsForGroup(group, authUser.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<Response<List<RecommendationResponse>>> getRecommendations(
            @PathVariable Long groupId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        RecommendGroup group = recommendService.findGroup(groupId);
        List<RecommendationResponse> result = recommendService.getRecommendationsForGroup(group, authUser.getId());
        return ResponseEntity.ok(Response.of(result));
    }
}
