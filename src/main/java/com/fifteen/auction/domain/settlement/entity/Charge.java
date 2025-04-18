package com.fifteen.auction.domain.settlement.entity;

import com.fifteen.auction.domain.settlement.enums.ChargeType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "charges")
public class Charge {

    @Id
    @Enumerated(EnumType.STRING)
    private ChargeType id;

    private BigDecimal proportion;

    public void updateProportion(BigDecimal proportion) {
        this.proportion = proportion;
    }
}
