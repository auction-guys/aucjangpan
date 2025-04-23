package com.fifteen.auction.domain.auction.controller;

import com.fifteen.auction.domain.auction.dto.request.BidRequest;
import com.fifteen.auction.domain.auction.dto.response.BidHistoryInfo;
import com.fifteen.auction.domain.auction.service.BidService;
import com.fifteen.auction.domain.user.auth.entity.AuthUser;
import com.fifteen.auction.global.dto.PageCond;
import com.fifteen.auction.global.dto.PageInfo;
import com.fifteen.auction.global.dto.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class BidController {
    private final BidService bidService;

    @PostMapping("/v1/auctions/{auctionSeq}/bids")
    public ResponseEntity<Void> bid(
            @PathVariable("auctionSeq") String auctionSeq,
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody BidRequest req
    ) {
        bidService.bid(auctionSeq, authUser.getId(), req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/v1/auctions/{auctionSeq}/buynow")
    public ResponseEntity<Void> buyNow(
            @PathVariable("auctionSeq") String auctionSeq,
            @AuthenticationPrincipal AuthUser authUser

    ) {
        bidService.buyNow(auctionSeq, authUser.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/v1/auctions/{auctionSeq}/bids")
    public ResponseEntity<Response<List<BidHistoryInfo>>> bidHistory(
            @PathVariable("auctionSeq") String auctionSeq,
            @ModelAttribute PageCond pageCond
    ) {
        Page<BidHistoryInfo> result = bidService.bidHistoriesInProgress(auctionSeq, pageCond);
        PageInfo pageInfo = PageInfo.builder()
                .pageNum(result.getNumber())
                .pageSize(result.getSize())
                .totalElement(result.getTotalElements())
                .totalPage(result.getTotalPages())
                .build();
        return ResponseEntity.ok(Response.of(result.getContent(), pageInfo));
    }
}
