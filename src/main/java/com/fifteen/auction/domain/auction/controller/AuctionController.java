package com.fifteen.auction.domain.auction.controller;

import com.fifteen.auction.domain.auction.dto.request.AuctionCreateRequest;
import com.fifteen.auction.domain.auction.dto.request.AuctionUpdateRequest;
import com.fifteen.auction.domain.auction.dto.response.AuctionDetail;
import com.fifteen.auction.domain.auction.dto.response.AuctionListItem;
import com.fifteen.auction.domain.auction.dto.response.AuctionLog;
import com.fifteen.auction.global.dto.PageCond;
import com.fifteen.auction.global.dto.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AuctionController {

    @PostMapping("/v1/auctions")
    ResponseEntity<Response<Void>> create(@RequestBody AuctionCreateRequest req) {
        return null;
    }

    // TODO: 조건 검색 추가
    @GetMapping("/v1/auctions")
    ResponseEntity<Response<List<AuctionListItem>>> findAll(@ModelAttribute PageCond cond) {
        return null;
    }

    @GetMapping("/v1/auctions/{auctionSeq}")
    ResponseEntity<Response<AuctionDetail>> findOne(@PathVariable("auctionSeq") String seq) {
        return null;
    }

    // TODO: 조건 검색 추가
    // TODO: AuthUser 적용
    @GetMapping("/v1/auctions/me")
    ResponseEntity<Response<List<AuctionLog>>> findOwnLog(@RequestParam("userId") Long userId) {
        return null;
    }

    // TODO: AuthUser 적용
    @PutMapping("/v1/auctions/{auctionSeq}/cancel")
    ResponseEntity<Response<Void>> cancel(
            @RequestParam("userId") Long userId,
            @PathVariable("auctionSeq") String auctionSeq
    ) {
        return null;
    }

    // TODO: AuthUser 적용
    @PutMapping("/v1/auctions/{auctionSeq}/open")
    ResponseEntity<Response<Void>> open(
            @RequestParam("userId") Long userId,
            @PathVariable("auctionSeq") String auctionSeq
    ) {
        return null;
    }

    // TODO: AuthUser 적용
    @PutMapping("/v1/auctions/{auctionSeq}/info")
    ResponseEntity<Response<Void>> updateInfo(
            @RequestParam("userId") Long userId,
            @PathVariable("auctionSeq") String auctionSeq,
            @RequestBody AuctionUpdateRequest req
    ) {
        return null;
    }

}
