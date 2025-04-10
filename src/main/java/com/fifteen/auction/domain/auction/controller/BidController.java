package com.fifteen.auction.domain.auction.controller;

import com.fifteen.auction.domain.auction.dto.request.DoBidRequest;
import com.fifteen.auction.domain.auction.service.BidService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class BidController {
    private final BidService bidService;

    @PostMapping("/v1/auctions/{auctionSeq}/bid")
    public ResponseEntity<Void> bid(
            @PathVariable("auctionSeq") String auctionSeq,
            @RequestParam("userId") Long userId,
            @RequestBody DoBidRequest req
    ) {
        bidService.bid(auctionSeq, userId, req);
        return ResponseEntity.ok().build();
    }
}
