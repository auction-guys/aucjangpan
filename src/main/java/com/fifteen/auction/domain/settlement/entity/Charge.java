package com.fifteen.auction.domain.settlement.entity;

import com.fifteen.auction.domain.settlement.enums.ChargeType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "charges")
public class Charge {

    @Id
    @Enumerated(EnumType.STRING)
    private ChargeType id;

    private double proportion;

    public void updateProportion(double proportion) {
        this.proportion = proportion;
    }
}
