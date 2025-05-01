package com.fifteen.auction.domain.settlement.dto.response;

import com.fifteen.auction.domain.settlement.entity.Settlement;
import com.fifteen.auction.domain.settlement.util.csv.RowMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class SettlementResponse implements RowMapper {
    private String settlementId;
    private String sellerId;
    private String orderId;
    private String amount;
    private String charge;
    private String settlementAmount;
    private String status;
    private String settlementDate;
    private String createdAt;
    private String bankAccount;

    public static SettlementResponse from(Settlement settlement) {
        return new SettlementResponse(
                settlement.getSettlementAmount().toString(),
                settlement.getOrder().getAuction().getProduct().getSeller().getId().toString(),
                settlement.getOrder().getId(),
                settlement.getOrder().getAuction().getWinPrice().toString(),
                settlement.getCharge().toString(),
                settlement.getSettlementAmount().toString(),
                settlement.getStatus().toString(),
                settlement.getSettledAt() == null ? "자동 정산 예정" : settlement.getSettledAt().toString(),
                settlement.getCreatedAt().toString(),
                settlement.getOrder().getAuction().getProduct().getSeller().getAccountNumber());
    }

    @Override
    public String[] toCsvRow() {
        return new String[] {
                this.getSettlementId(),
                this.getSellerId(),
                this.getOrderId(),
                this.getAmount(),
                this.getCharge(),
                this.getSettlementAmount(),
                this.getStatus(),
                this.getSettlementDate(),
                this.getCreatedAt(),
                this.getBankAccount()
        };
    }
}
