package com.fifteen.auction.domain.settlement.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SettlementResponse {
    private String settlementId;
    private String sellerId;
    private String orderId;
    private String amount;
    private String charge;
    private String settlementAmount;
    private String settlementDate;
    private String createdAt;
    private String bankAccount;

    @Builder
    public SettlementResponse(String settlementId, String sellerId, String orderId, String amount, String charge, String settlementAmount, String settlementDate, String createdAt, String bankAccount) {
        this.settlementId = settlementId;
        this.sellerId = sellerId;
        this.orderId = orderId;
        this.amount = amount;
        this.charge = charge;
        this.settlementAmount = settlementAmount;
        this.settlementDate = settlementDate;
        this.createdAt = createdAt;
        this.bankAccount = bankAccount;
    }
}
