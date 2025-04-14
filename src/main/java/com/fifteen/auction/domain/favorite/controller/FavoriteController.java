package com.fifteen.auction.domain.favorite.controller;

import com.fifteen.auction.domain.favorite.dto.response.FavoriteResponse;
import com.fifteen.auction.domain.favorite.service.FavoriteService;
import com.fifteen.auction.global.dto.PageCond;
import com.fifteen.auction.global.dto.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
            @RequestHeader("X-USER-ID") Long userId,
            @RequestParam Long auctionId
    ) {
        favoriteService.like(userId, auctionId);
        return ResponseEntity.ok().build();
    }

    // 찜 하기 취소
    @DeleteMapping
    public ResponseEntity<Void> unlike(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestParam Long auctionId
    ) {
        favoriteService.unlike(userId, auctionId);
        return ResponseEntity.noContent().build();
    }

    // 찜 목록 조회
    @GetMapping
    public ResponseEntity<Response<List<FavoriteResponse>>> getMyFavorites(
            @RequestHeader("X-USER-ID") Long userId,
            @ModelAttribute @Valid PageCond pageCond
    ) {
        return ResponseEntity.ok(favoriteService.findMyFavorites(userId, pageCond));
    }
}