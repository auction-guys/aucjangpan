package com.fifteen.auction.domain.settlement.entity;

import com.fifteen.auction.domain.order.entity.Order;
import com.fifteen.auction.domain.settlement.enums.SettlementStatus;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    private final LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime settledAt = null;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    public Settlement(Order order, BigDecimal proportion) {
        this.charge = new BigDecimal(String.valueOf(order.getAuction().getWinPrice())).multiply(proportion);
        this.settlementAmount = new BigDecimal(String.valueOf(order.getAuction().getWinPrice())).subtract(charge);
        this.order = order;
    }

    public void validateOwner(Long userId) {
        if (!this.order.getAuction().getProduct().getSeller().getId().equals(userId)) {
            throw new ClientException(ErrorCode.ORDER_ACCESS_DENIED);
        }
    }

    public void settled() {
        this.status = SettlementStatus.COMPLETED;
        this.settledAt = LocalDate.now().atStartOfDay();
    }

    public void settleNow(Long userId, BigDecimal proportion) {
        validateOwner(userId);
        this.charge = new BigDecimal(String.valueOf(this.order.getAuction().getWinPrice())).multiply(proportion);
        this.settlementAmount = new BigDecimal(this.order.getAuction().getWinPrice()).subtract(charge);
        this.status = SettlementStatus.IN_PROGRESS;
        this.settledAt = LocalDateTime.now().isAfter(LocalDateTime.now()
                .toLocalDate().atTime(3,59)) ? LocalDateTime.now().plusDays(1) : LocalDateTime.now();
    }
}
