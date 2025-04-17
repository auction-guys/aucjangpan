package com.fifteen.auction.domain.tag.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fifteen.auction.domain.auction.entity.Auction;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "auction_tag", uniqueConstraints = @UniqueConstraint(columnNames = {"auction_id", "tag_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuctionTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auction_id")
    private Auction auction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tag_id")
    private Tag tag;

    // 생성자
    private AuctionTag(Auction auction, Tag tag) {
        this.auction = auction;
        this.tag = tag;
    }

    public static AuctionTag create(Auction auction, Tag tag) {
        return new AuctionTag(auction, tag);
    }
}