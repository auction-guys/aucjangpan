package com.fifteen.auction.domain.chat.repository.message;

import com.fifteen.auction.domain.chat.entity.ChatMessage;
import com.fifteen.auction.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long>, ChatMessageCustomRepository {
    List<ChatMessage> findByChatRoomOrderByCreatedAtAsc(ChatRoom chatRoom);
}
