package com.fifteen.auction.domain.auction.controller;


import com.fifteen.auction.domain.auction.dto.request.BidRequest;
import com.fifteen.auction.domain.auction.service.BidService;
import com.fifteen.auction.domain.user.auth.entity.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class BidV2Controller {

    private final BidService bidService;

    @PostMapping("/v2/auctions/{auctionSeq}/bids")
    public ResponseEntity<Void> bid(
            @PathVariable("auctionSeq") String auctionSeq,
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody BidRequest req
    ) {
        bidService.putBidIntoQueue(auctionSeq, authUser.getId(), req.getPrice());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/v2/auctions/{auctionSeq}/buynow")
    public ResponseEntity<Void> buyNow(
            @PathVariable("auctionSeq") String auctionSeq,
            @AuthenticationPrincipal AuthUser authUser

    ) {
        bidService.putBuyNowIntoQueue(auctionSeq, authUser.getId());
        return ResponseEntity.ok().build();
    }
}
