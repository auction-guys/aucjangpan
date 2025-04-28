package com.fifteen.auction.domain.auction.controller;

import com.fifteen.auction.domain.auction.service.AuctionService;
import com.fifteen.auction.domain.user.auth.entity.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class AuctionV2Controller {

    private final AuctionService auctionService;

    @PutMapping("/v2/auctions/{auctionSeq}/open")
    ResponseEntity<Void> open(
            @PathVariable("auctionSeq") String auctionSeq,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        auctionService.openV2(auctionSeq, authUser.getId());
        return ResponseEntity.noContent().build();
    }
}
