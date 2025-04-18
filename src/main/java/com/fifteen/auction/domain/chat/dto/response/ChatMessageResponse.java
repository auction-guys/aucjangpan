package com.fifteen.auction.domain.chat.dto.response;

import com.fifteen.auction.domain.chat.entity.ChatMessage;
import com.fifteen.auction.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Builder
public class ChatMessageResponse {
    private final Long senderId;
    @Setter
    private String nickname;
    private final String content;
    private final Long chatRoomId;
    private final LocalDateTime createdAt;

    public ChatMessageResponse(Long senderId, String nickname, String content, Long chatRoomId, LocalDateTime createdAt) {
        this.senderId = senderId;
        this.nickname = nickname;
        this.content = content;
        this.chatRoomId = chatRoomId;
        this.createdAt = createdAt;
    }

    public static ChatMessageResponse fromEntity(ChatMessage chatMessage, User user) {
        return ChatMessageResponse.builder()
                .senderId(chatMessage.getSenderId())
                .nickname(user.getNickname()) // senderId로 조회한 User의 닉네임
                .content(chatMessage.getContent())
                .chatRoomId(chatMessage.getChatRoom().getId())
                .createdAt(chatMessage.getCreatedAt())
                .build();
    }
}
