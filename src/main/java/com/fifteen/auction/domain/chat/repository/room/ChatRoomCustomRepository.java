package com.fifteen.auction.domain.chat.repository.room;

import com.fifteen.auction.domain.chat.dto.response.ChatRoomListResponse;
import com.fifteen.auction.domain.chat.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ChatRoomCustomRepository {
    Optional<ChatRoom> findByUserIdAndSellerId(Long userId, Long sellerId);
    Page<ChatRoomListResponse> findChatRoomsByUserId(Long userId, Pageable pageable);
}
