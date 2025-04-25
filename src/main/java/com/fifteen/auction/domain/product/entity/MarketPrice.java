package com.fifteen.auction.domain.product.entity;

import com.fifteen.auction.domain.product.enums.PriceType;
import com.fifteen.auction.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class MarketPrice extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    private LocalDate priceDate;
    private Long minMarketPrice;
    private Long maxMarketPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PriceType priceType;


}