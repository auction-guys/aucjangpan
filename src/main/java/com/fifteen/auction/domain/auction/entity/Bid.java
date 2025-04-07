package com.fifteen.auction.domain.auction.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "bids")
public class Bid {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id")
    private Auction auction;

    private Long bidderId;

    private Long bidPrice;

    private LocalDateTime bidAt;

    public Bid(Auction auction, Long bidderId, Long bidPrice, LocalDateTime bidAt) {
        this.auction = auction;
        this.bidderId = bidderId;
        this.bidPrice = bidPrice;
        this.bidAt = bidAt;
    }
}
