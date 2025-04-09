package com.fifteen.auction.domain.settlement.entity;

import com.fifteen.auction.domain.order.entity.Order;
import com.fifteen.auction.domain.settlement.enums.SettlementStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "settlements")
public class Settlement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(precision = 11, scale = 2)
    private BigDecimal charge;
    @Column(precision = 11, scale = 2)
    private BigDecimal settlementAmount;
    @Enumerated(EnumType.STRING)
    private SettlementStatus status = SettlementStatus.PENDING;
    private Long sellerId;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime settledAt = null;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    public Settlement(Order order) {
        this.charge = new BigDecimal(String.valueOf(order.getAuction().getWinPrice() * 0.1)); // 수수료는 나중에 환경변수 같은걸로 설정
        this.settlementAmount = new BigDecimal(String.valueOf(order.getAuction().getWinPrice())).subtract(charge);
        this.sellerId = order.getAuction().getProduct().getSeller().getId();
        this.order = order;
    }

    public void settled() {
        this.status = SettlementStatus.IN_PROGRESS;
        this.settledAt = LocalDate.now().atStartOfDay();
    }

    public void settleNow(Long winPrice) {
        this.charge = new BigDecimal(String.valueOf(winPrice * 0.2)); // 수수료는 나중에 환경변수 같은걸로 설정
        this.settlementAmount = new BigDecimal(winPrice).subtract(charge);
        this.status = SettlementStatus.IN_PROGRESS;
        this.settledAt = LocalDate.now().atStartOfDay();
    }

    public void settleNow(Long winPrice){
        this.charge = new BigDecimal(String.valueOf(winPrice * 0.2)); // 수수료는 나중에 환경변수 같은걸로 설정
        this.settlementAmount = new BigDecimal(winPrice).subtract(charge);
        this.status = SettlementStatus.IN_PROGRESS;
        this.settled_at = LocalDate.now().atStartOfDay();
    }
}
