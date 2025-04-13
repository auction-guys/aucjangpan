package com.fifteen.auction.domain.settlement.dto.response;

import com.fifteen.auction.domain.settlement.entity.Settlement;
import com.fifteen.auction.domain.settlement.util.RowMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SettlementResponse implements RowMapper {
    private String settlementId;
    private String sellerId;
    private String orderId;
    private String amount;
    private String charge;
    private String settlementAmount;
    private String settlementDate;
    private String createdAt;
    private String bankAccount;

    public static SettlementResponse from(Settlement settlement) {
        return new SettlementResponse(
                settlement.getSettlementAmount().toString(),
                settlement.getOrder().getAuction().getProduct().getSeller().getId().toString(),
                settlement.getOrder().getId().toString(),
                settlement.getOrder().getAuction().getWinPrice().toString(),
                settlement.getCharge().toString(),
                settlement.getSettlementAmount().toString(),
                settlement.getSettledAt().toString(),
                settlement.getCreatedAt().toString(),
                settlement.getOrder().getAuction().getProduct().getSeller().getAccountNumber());
    }

    @Override
    public String[] toCsvRow() {
        return new String[] {
                String.valueOf(this.getSettlementId()),
                String.valueOf(this.getSellerId()),
                String.valueOf(this.getOrderId()),
                String.valueOf(this.getAmount()),
                String.valueOf(this.getCharge()),
                String.valueOf(this.getSettlementAmount()),
                String.valueOf(this.getSettlementDate()),
                String.valueOf(this.getCreatedAt()),
                String.valueOf(this.getBankAccount())
        };
    }
}
