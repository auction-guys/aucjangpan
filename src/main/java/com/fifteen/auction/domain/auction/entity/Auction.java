package com.fifteen.auction.domain.auction.entity;

import com.fifteen.auction.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Auction extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Todo: Product

    private Long winnerId;

    private String auctionNum;

    private Long startPrice;

    private Long buyNowPrice;

    private Long winPrice;

    private int bidUnit;

    private int views;

    private boolean isBuyNowSet;

    private boolean isAutoExtensible;

    private AuctionStatus status;

    private LocalDateTime doneAt;

    private LocalDateTime expiresAt;

    public Auction(
            String auctionNum, Long startPrice, Long buyNowPrice, int bidUnit,
            boolean isBuyNowSet, boolean isAutoExtensible, LocalDateTime expiresAt
    ) {
        this.auctionNum = auctionNum;
        this.startPrice = startPrice;
        this.buyNowPrice = buyNowPrice;
        this.bidUnit = bidUnit;
        this.isBuyNowSet = isBuyNowSet;
        this.isAutoExtensible = isAutoExtensible;
        this.expiresAt = expiresAt;
        this.status = AuctionStatus.PENDING;
    }

    public void open() {
        this.status = AuctionStatus.OPEN;
    }

    public void finalize(Long winnerId, Long winPrice, LocalDateTime doneAt) {
        this.winnerId = winnerId;
        this.winPrice = winPrice;
        this.doneAt = doneAt;
        this.status = AuctionStatus.DONE;
    }

    public void buyNow(Long winnerId, LocalDateTime doneAt) {
        this.winnerId = winnerId;
        this.winPrice = this.buyNowPrice;
        this.doneAt = doneAt;
        this.status = AuctionStatus.DONE;
    }

    public void misCarry() {
        this.status = AuctionStatus.MISCARRY;
        this.doneAt = this.expiresAt;
    }
}
