package com.fifteen.auction.domain.chat.dto.response;


import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatRoomListResponse {
    private Long chatRoomId;
    private Long opponentUserId;
    private String opponentNickname;

    public ChatRoomListResponse(Long chatRoomId, Long opponentUserId, String opponentNickname){
        this.chatRoomId = chatRoomId;
        this.opponentUserId = opponentUserId;
        this.opponentNickname = opponentNickname;
    }
}
