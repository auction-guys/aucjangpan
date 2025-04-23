package com.fifteen.auction.domain.settlement.controller;

import com.fifteen.auction.domain.settlement.dto.response.SettlementResponse;
import com.fifteen.auction.domain.settlement.service.SettlementService;
import com.fifteen.auction.domain.user.auth.entity.AuthUser;
import com.fifteen.auction.global.dto.PageCond;
import com.fifteen.auction.global.dto.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @PostMapping("api/v1/settlements")
    public ResponseEntity<Response<String>> settle() {
        // TODO: 나중에 시큐리티로 어드민만 가능하게

        return ResponseEntity.ok(Response.of(settlementService.settleMonthly()));
    }

    @PostMapping("api/v1/settlements/{settlementId}")
    public ResponseEntity<Response<SettlementResponse>> settleImmediately(
//            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long settlementId) {
        Long currentUserId = 1L;

        SettlementResponse dto = settlementService.settleImmediately(settlementId, currentUserId);

        settlementService.csvUpload(dto);

        return ResponseEntity.ok(Response.of(dto));
    }

    @GetMapping("api/v1/settlements")
    public ResponseEntity<Response<Page<SettlementResponse>>> findSettlements(
//            @AuthenticationPrincipal AuthUser authUser,
            @ModelAttribute PageCond pageCond) {
        Long currentUserId = 1L;

        return ResponseEntity.ok(settlementService.findSettlements(currentUserId, pageCond));
    }

    @GetMapping("api/v1/settlements/{settlementId}")
    public ResponseEntity<Response<SettlementResponse>> findSettlement(
//            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long settlementId) {
        Long currentUserId = 1L;
        return ResponseEntity.ok(Response.of(settlementService.findSettlement(currentUserId, settlementId)));
    }
}
