package com.fifteen.auction.domain.favorite.entity;

import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.domain.user.entity.User;
import com.fifteen.auction.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "favorite",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "auction_id"})})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Favorite extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auction_id")
    private Auction auction;

    private Favorite(User user, Auction auction) {
        this.user = user;
        this.auction = auction;
    }

    public static Favorite create(User user, Auction auction) {
        return new Favorite(user, auction);
    }
}