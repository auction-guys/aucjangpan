package com.fifteen.auction.domain.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
public class MarketPrice extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "product_id")
    private Product product;
    private Long minMarketPrice;
    private Long maxMarketPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
