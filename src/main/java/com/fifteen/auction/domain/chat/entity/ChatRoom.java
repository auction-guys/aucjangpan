package com.fifteen.auction.domain.chat.entity;

import com.fifteen.auction.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name="ChatRoom")
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long sellerId;

    public ChatRoom(Long userId, Long sellerId) {
        this.userId = userId;
        this.sellerId = sellerId;
    }
}
