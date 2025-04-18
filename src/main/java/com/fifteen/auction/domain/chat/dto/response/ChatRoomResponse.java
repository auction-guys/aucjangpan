package com.fifteen.auction.domain.chat.dto.response;

import com.fifteen.auction.domain.chat.entity.ChatRoom;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class ChatRoomResponse {
    private Long userId;
    private Long sellerId;
    private Long chatRoomId;
    private List<ChatMessageResponse> messages;

    public static ChatRoomResponse fromEntity(ChatRoom chatRoom, List<ChatMessageResponse> messageDtos) {
        return ChatRoomResponse.builder()
                .userId(chatRoom.getUserId())
                .sellerId(chatRoom.getSellerId())
                .chatRoomId(chatRoom.getId())
                .messages(messageDtos)
                .build();
    }
}
