package com.fifteen.auction.domain.settlement.controller;

import com.fifteen.auction.domain.settlement.dto.response.SettlementResponse;
import com.fifteen.auction.domain.settlement.service.SettlementService;
import com.fifteen.auction.global.dto.PageCond;
import com.fifteen.auction.global.dto.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @PostMapping("api/v1/settlements")
    public ResponseEntity<Void> settle() {
        // 나중에 시큐리티로 어드민만 가능하게
        settlementService.settle();

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("api/v1/settlements/{settlementId}")
    public ResponseEntity<Void> settleImmediately(
            @PathVariable Long settlementId,
            Long currentUserId) {
        settlementService.settleImmediately(settlementId, currentUserId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("api/v1/settlements")
    public ResponseEntity<Response<Page<SettlementResponse>>> findSettlements(
            Long currentUserId,
            PageCond pageCond) {

        return ResponseEntity.ok(settlementService.findSettlements(currentUserId, pageCond));
    }

    @GetMapping("api/v1/settlements/{settlementId}")
    public ResponseEntity<Response<SettlementResponse>> findSettlement(
            Long currentUserId,
            @PathVariable Long settlementId) {

        return ResponseEntity.ok(Response.of(settlementService.findSettlement(currentUserId, settlementId)));
    }

}
