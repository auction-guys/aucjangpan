package com.fifteen.auction.domain.chat.repository.message;

import com.fifteen.auction.domain.chat.dto.response.ChatMessageResponse;
import com.fifteen.auction.domain.chat.entity.ChatRoom;

import java.util.List;

public interface ChatMessageCustomRepository {
    List<ChatMessageResponse> findMessagesWithUserByChatRoom(ChatRoom chatRoom);
}
