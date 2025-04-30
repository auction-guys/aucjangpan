package com.fifteen.auction.domain.auction.controller;

import com.fifteen.auction.domain.auction.dto.response.CurrentPriceInfo;
import com.fifteen.auction.domain.auction.service.AuctionService;
import com.fifteen.auction.domain.user.auth.entity.AuthUser;
import com.fifteen.auction.global.dto.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping()
@RestController
public class AuctionV2Controller {

    private final AuctionService auctionService;

    @PutMapping("/api/v2/auctions/{auctionSeq}/open")
    ResponseEntity<Void> open(
            @PathVariable("auctionSeq") String auctionSeq,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        auctionService.openV2(auctionSeq, authUser.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/internal/v2/auctions/{auctionSeq}/currentPrice")
    ResponseEntity<Response<CurrentPriceInfo>> findCurrentPrice(
            @PathVariable("auctionSeq") String auctionSeq
    ) {
        CurrentPriceInfo currentPrice = auctionService.findCurrentPrice(auctionSeq);
        return ResponseEntity.ok(Response.of(currentPrice));
    }
}
