package com.fifteen.auction.domain.auction.controller;

import com.fifteen.auction.domain.auction.dto.request.AuctionCreateRequest;
import com.fifteen.auction.domain.auction.dto.request.AuctionUpdateRequest;
import com.fifteen.auction.domain.auction.dto.response.AuctionDetail;
import com.fifteen.auction.domain.auction.dto.response.AuctionListItem;
import com.fifteen.auction.domain.auction.dto.response.AuctionLog;
import com.fifteen.auction.domain.auction.service.AuctionService;
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

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class AuctionController {

    private final AuctionService auctionService;

    @PostMapping("/v1/auctions")
    ResponseEntity<Object> create(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody AuctionCreateRequest req
    ) {
        String auctionSeq = auctionService.create(req, authUser.getId());
        return ResponseEntity
                .created(URI.create("/api/v1/auctions/" + auctionSeq))
                .build();
    }

    // TODO: 조건 검색 추가
    @GetMapping("/v1/auctions")
    ResponseEntity<Response<List<AuctionListItem>>> findAll(@ModelAttribute PageCond cond) {
        Page<AuctionListItem> result = auctionService.findAll(cond);

        return ResponseEntity.ok(Response.of(result.getContent(), PageInfo.fromPage(result)));
    }

    @GetMapping("/v1/auctions/{auctionSeq}")
    ResponseEntity<Response<AuctionDetail>> findOne(@PathVariable("auctionSeq") String seq) {
        return ResponseEntity.ok(Response.of(auctionService.findOneAndIncreaseView(seq)));
    }

    // TODO: 조건 검색 추가
    @GetMapping("/v1/auctions/me")
    ResponseEntity<Response<List<AuctionLog>>> findAllMyLog(
            @AuthenticationPrincipal AuthUser authUser,
            @ModelAttribute PageCond cond
    ) {
        Page<AuctionLog> result = auctionService.findJoinedAuction(cond, authUser.getId());

        return ResponseEntity.ok(Response.of(result.getContent(), PageInfo.fromPage(result)));
    }

    @PutMapping("/v1/auctions/{auctionSeq}/cancel")
    ResponseEntity<Void> cancel(
            @PathVariable("auctionSeq") String auctionSeq,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        auctionService.cancel(auctionSeq, authUser.getId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/v1/auctions/{auctionSeq}/open")
    ResponseEntity<Void> open(
            @PathVariable("auctionSeq") String auctionSeq,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        auctionService.open(auctionSeq, authUser.getId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/v1/auctions/{auctionSeq}/info")
    ResponseEntity<Response<Void>> updateInfo(
            @PathVariable("auctionSeq") String auctionSeq,
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody AuctionUpdateRequest req
    ) {
        auctionService.updateInfo(auctionSeq, authUser.getId(), req);
        return ResponseEntity.noContent().build();
    }

}
