package com.fifteen.auction.domain.settlement.entity;

import com.fifteen.auction.domain.order.entity.Order;
import com.fifteen.auction.domain.settlement.enums.SettlementStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.text.DecimalFormat;
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
    private BigDecimal settlement_amount;
    @Enumerated(EnumType.STRING)
    private SettlementStatus status = SettlementStatus.PENDING;
    private LocalDateTime created_at = LocalDateTime.now();
    private LocalDateTime settled_at = null;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    public Settlement(Order order) {
        this.charge = new BigDecimal(String.valueOf(order.getAuction().getWinPrice() * 0.1));
        this.settlement_amount = new BigDecimal(String.valueOf(order.getAuction().getWinPrice())).subtract(charge);
        this.order = order;
    }
}
