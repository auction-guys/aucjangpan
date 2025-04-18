package com.fifteen.auction.domain.settlement.controller;

import com.fifteen.auction.domain.settlement.service.ChargeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
public class ChargeController {

    private final ChargeService chargeService;

    @PatchMapping("/api/v1/charges/{chargeId}")
    public ResponseEntity<Void> updateCharge(
            @PathVariable String chargeId,
            @RequestParam BigDecimal proportion){

        chargeService.updateCharge(chargeId, proportion);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
