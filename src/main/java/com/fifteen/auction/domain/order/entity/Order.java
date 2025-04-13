package com.fifteen.auction.domain.order.entity;

import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.domain.order.enums.OrderStatus;
import com.fifteen.auction.domain.user.entity.User;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import com.fifteen.auction.global.dto.exception.ServerException;
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

    private void validateOwner(Long userId) {
        if (!this.user.getId().equals(userId)) {
            throw new ClientException(ErrorCode.ORDER_ACCESS_DENIED);
        }
    }

    private void validateCancelable() {
        if (this.status == OrderStatus.CANCELED || this.status == OrderStatus.REFUNDED || this.status == OrderStatus.COMPLETED) {
            throw new ClientException(ErrorCode.ORDER_CANNOT_BE_CANCELED);
        }
    }

    private void validateConfirmable() {
        if (this.status == OrderStatus.CANCELED || this.status == OrderStatus.REFUNDED || this.status == OrderStatus.COMPLETED) {
            throw new ClientException(ErrorCode.ORDER_CANNOT_BE_CANCELED);
        }
    }

    public void cancel(Long userId) {
        validateOwner(userId);
        validateCancelable();
        this.status = OrderStatus.CANCELED;
    }

    public void confirm(Long userId) {
        validateOwner(userId);
        validateConfirmable();
        this.status = OrderStatus.CONFIRMED;
    }

    public void paid() {
        this.status = OrderStatus.PAID;
    }

    public void validatePaymentInfo( Long userId, Long amount) {
        if (!this.getUser().getId().equals(userId) && this.getAuction().getWinPrice().equals(amount)) {
            System.out.println("       "+userId+"    "+this.getUser().getId()+"       "+amount+"      "+this.getAuction().getWinPrice());
            throw new ServerException(ErrorCode.PAYMENT_INFO_NOT_MATCHED);
        }
    }
}