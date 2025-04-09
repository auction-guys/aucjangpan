package com.fifteen.auction.domain.settlement.controller;

import com.fifteen.auction.domain.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @PostMapping("api/v1/settlements")
    public ResponseEntity<Void> settle(){
        // 나중에 시큐리티로 어드민만 가능하게
        settlementService.settle();

        return new ResponseEntity<>(HttpStatus.OK);
    }



}
