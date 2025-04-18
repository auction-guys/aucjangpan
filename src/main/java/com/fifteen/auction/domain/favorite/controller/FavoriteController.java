package com.fifteen.auction.domain.favorite.controller;

import com.fifteen.auction.domain.favorite.dto.response.FavoriteResponse;
import com.fifteen.auction.domain.favorite.service.FavoriteService;
import com.fifteen.auction.domain.user.auth.entity.AuthUser;
import com.fifteen.auction.global.dto.PageCond;
import com.fifteen.auction.global.dto.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    // 찜 하기
    @PostMapping
    public ResponseEntity<Void> like(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam Long auctionId
    ) {
        favoriteService.like(authUser.getId(), auctionId);
        return ResponseEntity.ok().build();
    }

    // 찜 하기 취소
    @DeleteMapping
    public ResponseEntity<Void> unlike(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam Long auctionId
    ) {
        favoriteService.unlike(authUser.getId(), auctionId);
        return ResponseEntity.noContent().build();
    }

    // 찜 목록 조회
    @GetMapping
    public ResponseEntity<Response<List<FavoriteResponse>>> getMyFavorites(
            @AuthenticationPrincipal AuthUser authUser,
            @ModelAttribute @Valid PageCond pageCond
    ) {
        return ResponseEntity.ok(favoriteService.findMyFavorites(authUser.getId(), pageCond));
    }
}