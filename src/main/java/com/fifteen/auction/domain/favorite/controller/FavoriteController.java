package com.fifteen.auction.domain.favorite.controller;

import com.fifteen.auction.domain.favorite.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PatchMapping("/{auctionId}")
    public ResponseEntity<Void> toggleFavorite(
            @RequestHeader("X-USER-ID") Long userId,
            @PathVariable Long auctionId
    ) {
        favoriteService.toggleFavorite(userId, auctionId);
        return ResponseEntity.ok().build();
    }

//    @GetMapping
//    public ResponseEntity<Response<List<FavoriteResponse>>> getMyFavorites(
//            @RequestHeader("X-USER-ID") Long userId
//    ) {
//        List<Favorite> favorites = favoriteService.getMyFavorites(userId);
//        List<FavoriteResponse> response = favorites.stream()
//                .map(FavoriteResponse::from)
//                .collect(Collectors.toList());
//
//        return ResponseEntity.ok(Response.of(response));
//    }
}