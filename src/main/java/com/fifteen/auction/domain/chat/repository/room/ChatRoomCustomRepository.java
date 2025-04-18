package com.fifteen.auction.domain.chat.repository.room;

import com.fifteen.auction.domain.chat.entity.ChatRoom;

import java.util.Optional;

public interface ChatRoomCustomRepository {
    Optional<ChatRoom> findByUserIdAndSellerId(Long userId, Long sellerId);
}
