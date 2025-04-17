package com.fifteen.auction.domain.auction.controller;

import com.fifteen.auction.domain.auction.dto.request.AuctionCreateRequest;
import com.fifteen.auction.domain.auction.dto.request.AuctionUpdateRequest;
import com.fifteen.auction.domain.auction.dto.response.AuctionDetail;
import com.fifteen.auction.domain.auction.dto.response.AuctionListItem;
import com.fifteen.auction.domain.auction.dto.response.AuctionLog;
import com.fifteen.auction.domain.auction.service.AuctionService;
import com.fifteen.auction.global.dto.PageCond;
import com.fifteen.auction.global.dto.PageInfo;
import com.fifteen.auction.global.dto.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class AuctionController {

    private final AuctionService auctionService;

    // TODO: AuthUser 적용
    @PostMapping("/v1/auctions")
    ResponseEntity<Object> create(@RequestBody AuctionCreateRequest req, @RequestParam("userId") Long userId) {
        String auctionSeq = auctionService.create(req, userId);
        return ResponseEntity
                .created(URI.create("/api/v1/auctions/" + auctionSeq))
                .build();
    }

    // TODO: 조건 검색 추가
    @GetMapping("/v1/auctions")
    ResponseEntity<Response<List<AuctionListItem>>> findAll(@ModelAttribute PageCond cond) {
        Page<AuctionListItem> result = auctionService.findAll(cond);
        PageInfo pageInfo = PageInfo.builder()
                .pageNum(result.getNumber())
                .pageSize(result.getSize())
                .totalElement(result.getTotalElements())
                .totalPage(result.getTotalPages())
                .build();

        return ResponseEntity.ok(Response.of(result.getContent(), pageInfo));
    }

    @GetMapping("/v1/auctions/{auctionSeq}")
    ResponseEntity<Response<AuctionDetail>> findOne(@PathVariable("auctionSeq") String seq) {
        return ResponseEntity.ok(Response.of(auctionService.findOne(seq)));
    }

    // TODO: 조건 검색 추가
    // TODO: AuthUser 적용
    @GetMapping("/v1/auctions/me")
    ResponseEntity<Response<List<AuctionLog>>> findAllMyLog(@RequestParam("userId") Long userId) {
        return null;
    }

    // TODO: AuthUser 적용
    @PutMapping("/v1/auctions/{auctionSeq}/cancel")
    ResponseEntity<Void> cancel(
            @PathVariable("auctionSeq") String auctionSeq,
            @RequestParam("userId") Long userId
    ) {
        auctionService.cancel(auctionSeq, userId);
        return ResponseEntity.noContent().build();
    }

    // TODO: AuthUser 적용
    @PutMapping("/v1/auctions/{auctionSeq}/open")
    ResponseEntity<Void> open(
            @PathVariable("auctionSeq") String auctionSeq,
            @RequestParam("userId") Long userId
    ) {
        auctionService.open(auctionSeq, userId);
        return ResponseEntity.noContent().build();
    }

    // TODO: AuthUser 적용
    @PutMapping("/v1/auctions/{auctionSeq}/info")
    ResponseEntity<Response<Void>> updateInfo(
            @PathVariable("auctionSeq") String auctionSeq,
            @RequestParam("userId") Long userId,
            @RequestBody AuctionUpdateRequest req
    ) {
        auctionService.updateInfo(auctionSeq, userId, req);
        return ResponseEntity.noContent().build();
    }

}
