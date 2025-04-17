package com.fifteen.auction.domain.settlement.dto.request;

import lombok.Getter;

@Getter
public class UpdateChargeRequest {
    private String chargeType;
    private double proportion;
}
