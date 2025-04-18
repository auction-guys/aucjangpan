package com.fifteen.auction.domain.chat.repository.room;

import com.fifteen.auction.domain.chat.entity.ChatRoom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

import java.util.Optional;

import static com.fifteen.auction.domain.chat.entity.QChatRoom.chatRoom;

public class ChatRoomCustomRepositoryImpl implements ChatRoomCustomRepository{

    private final JPAQueryFactory jpaQueryFactory;

    public ChatRoomCustomRepositoryImpl(EntityManager em) {
        jpaQueryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Optional<ChatRoom> findByUserIdAndSellerId(Long userId, Long sellerId) {

        ChatRoom room = jpaQueryFactory
                .selectFrom(chatRoom)
                .where(chatRoom.userId.eq(userId)
                        .and(chatRoom.sellerId.eq(sellerId)))
                .fetchFirst();

        if (room == null) {
            room = jpaQueryFactory
                    .selectFrom(chatRoom)
                    .where(chatRoom.sellerId.eq(userId)
                            .and(chatRoom.userId.eq(sellerId)))
                    .fetchFirst();
        }

        return Optional.ofNullable(room);
    }
}
