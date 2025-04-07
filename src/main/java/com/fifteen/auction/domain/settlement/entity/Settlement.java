package com.fifteen.auction.domain.settlement.entity;

import com.fifteen.auction.domain.order.entity.Order;
import com.fifteen.auction.domain.settlement.enums.SettlementStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "settlements")
public class Settlement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int charge;
    private int settlement_amount;
    @Enumerated(EnumType.STRING)
    private SettlementStatus status = SettlementStatus.PENDING;
    private LocalDateTime created_at;
    private LocalDateTime settled_at;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
}
