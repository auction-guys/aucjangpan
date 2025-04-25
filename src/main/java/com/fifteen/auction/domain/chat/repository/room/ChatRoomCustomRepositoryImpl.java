package com.fifteen.auction.domain.chat.repository.room;

import com.fifteen.auction.domain.chat.dto.response.ChatRoomListResponse;
import com.fifteen.auction.domain.chat.entity.ChatRoom;
import com.fifteen.auction.domain.user.entity.QUser;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
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

    @Override
    public Page<ChatRoomListResponse> findChatRoomsByUserId(Long userId, Pageable pageable) {
        QUser user = new QUser("user");
        QUser seller = new QUser("seller");

        List<ChatRoomListResponse> content = jpaQueryFactory
                .select(Projections.constructor(ChatRoomListResponse.class,
                        chatRoom.id,
                        new CaseBuilder()
                                .when(chatRoom.userId.eq(userId)).then(seller.id)
                                .otherwise(user.id)
                                .as("opponentUserId"),
                        new CaseBuilder()
                                .when(chatRoom.userId.eq(userId)).then(seller.nickname)
                                .otherwise(user.nickname)
                                .as("opponentNickname")
                ))
                .from(chatRoom)
                .join(user).on(chatRoom.userId.eq(user.id))
                .join(seller).on(chatRoom.sellerId.eq(seller.id))
                .where(chatRoom.userId.eq(userId).or(chatRoom.sellerId.eq(userId)))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = jpaQueryFactory
                .select(chatRoom.count())
                .from(chatRoom)
                .where(chatRoom.userId.eq(userId).or(chatRoom.sellerId.eq(userId)))
                .fetchOne();

        long totalCount = (total == null) ? 0 : total;

        return new PageImpl<>(content, pageable, totalCount);
    }
}
