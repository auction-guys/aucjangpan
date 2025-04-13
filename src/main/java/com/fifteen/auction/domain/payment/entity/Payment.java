package com.fifteen.auction.domain.payment.entity;

import com.fifteen.auction.domain.order.entity.Order;
import com.fifteen.auction.domain.payment.enums.PaymentStatus;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.json.simple.JSONObject;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String mid;
    private String paymentKey;
    private String paymentMethod = "Pending";
    private Long amount;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.READY;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Builder
    public Payment(String mid, String paymentKey, String paymentMethod, Long amount, PaymentStatus status, LocalDateTime requestedAt, LocalDateTime approvedAt, Order order) {
        this.mid = mid;
        this.paymentKey = paymentKey;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.status = status;
        this.requestedAt = requestedAt;
        this.approvedAt = approvedAt;
        this.order = order;
    }

    public Payment(JSONObject jsonObject, Order order) {
        this.mid = jsonObject.get("mId").toString();
        this.paymentKey = jsonObject.get("paymentKey").toString();
        this.paymentMethod = jsonObject.get("method").toString();
        JSONObject card = (JSONObject) jsonObject.get("card");
        this.amount = Long.parseLong(card.get("amount").toString());
        this.status = PaymentStatus.valueOf(jsonObject.get("status").toString());
        this.requestedAt = LocalDateTime.parse(jsonObject.get("requestedAt").toString().substring(0, 19));
        this.approvedAt = LocalDateTime.parse(jsonObject.get("approvedAt").toString().substring(0, 19));
        this.order = order;
    }


    public void validateOwner(Long userId) {
        if (!this.order.getUser().getId().equals(userId)) {
            throw new ClientException(ErrorCode.PAYMENT_ACCESS_DENIED);
        }
    }

    public void cancel() {
        this.status = PaymentStatus.CANCELED;
    }
}
