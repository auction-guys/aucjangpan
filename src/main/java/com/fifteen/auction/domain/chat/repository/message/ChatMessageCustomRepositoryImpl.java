package com.fifteen.auction.domain.chat.repository.message;

import com.fifteen.auction.domain.chat.dto.response.ChatMessageResponse;
import com.fifteen.auction.domain.chat.entity.ChatRoom;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;

import static com.fifteen.auction.domain.chat.entity.QChatMessage.chatMessage;
import static com.fifteen.auction.domain.user.entity.QUser.user;

public class ChatMessageCustomRepositoryImpl implements ChatMessageCustomRepository{

    private final JPAQueryFactory jpaQueryFactory;
    public ChatMessageCustomRepositoryImpl(EntityManager entityManager) {
        this.jpaQueryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public List<ChatMessageResponse> findMessagesWithUserByChatRoom(ChatRoom chatRoom) {

        return jpaQueryFactory
                .select(Projections.constructor(ChatMessageResponse.class,
                        chatMessage.senderId,
                        user.nickname,
                        chatMessage.content,
                        chatMessage.chatRoom.id,
                        chatMessage.createdAt
                ))
                .from(chatMessage)
                .join(user).on(chatMessage.senderId.eq(user.id))
                .where(chatMessage.chatRoom.eq(chatRoom))
                .orderBy(chatMessage.createdAt.asc())
                .fetch();
    }
}
