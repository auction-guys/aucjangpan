package com.fifteen.auction.domain.chat.dto.response;

import com.fifteen.auction.domain.chat.entity.ChatRoom;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateChatRoomResponse {
    private Long userId;
    private Long sellerId;
    private Long chatRoomId;

    public static CreateChatRoomResponse fromEntity(ChatRoom chatRoom){
        return CreateChatRoomResponse.builder()
                .userId(chatRoom.getUserId())
                .sellerId(chatRoom.getSellerId())
                .chatRoomId(chatRoom.getId())
                .build();
    }
}
