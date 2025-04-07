package com.fifteen.auction.domain.order.entity;

import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.domain.order.enums.OrderStatus;
import com.fifteen.auction.global.entity.BaseEntity;
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id")
    private Auction auction;
}
