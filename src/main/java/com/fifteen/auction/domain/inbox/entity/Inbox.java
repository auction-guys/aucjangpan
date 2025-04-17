package com.fifteen.auction.domain.inbox.entity;

import com.fifteen.auction.domain.user.entity.User;
import com.fifteen.auction.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SoftDelete;

@Entity
@Getter
@SoftDelete(columnName = "deleted")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inbox extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private MessageType type;

    private String message;

    public Inbox(User user, MessageType type, String message) {
        this.user = user;
        this.type = type;
        this.message = message;
    }
}
