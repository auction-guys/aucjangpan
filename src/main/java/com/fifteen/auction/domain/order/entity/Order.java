package com.fifteen.auction.domain.order.entity;

import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.domain.order.enums.OrderStatus;
import com.fifteen.auction.domain.user.entity.User;
import com.fifteen.auction.global.entity.BaseEntity;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor
@Table(name = "orders")
public class Order extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 이후 아이디 문자열로 바꿀 예정 관련해서 레디스 활용 (고도화 때 할 예정)
    private Long id;
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    private final String idempotencyKey = UUID.randomUUID().toString();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id")
    private Auction auction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Order(Auction auction, User user) {
        this.auction = auction;
        this.user = user;
    }

    public void cancel() {
        this.status = OrderStatus.CANCELED;
    }

    public void confirm() {
        this.status = OrderStatus.CONFIRMED;
    }

    public void paid() {
        this.status = OrderStatus.PAID;
    }
}
