package com.fifteen.auction.domain.chat.service;

import com.fifteen.auction.domain.chat.dto.request.ChatMessageRequest;
import com.fifteen.auction.domain.chat.dto.response.ChatMessageResponse;
import com.fifteen.auction.domain.chat.entity.ChatMessage;
import com.fifteen.auction.domain.chat.entity.ChatRoom;
import com.fifteen.auction.domain.chat.repository.message.ChatMessageRepository;
import com.fifteen.auction.domain.chat.repository.room.ChatRoomRepository;
import com.fifteen.auction.domain.user.entity.User;
import com.fifteen.auction.domain.user.repository.UserRepository;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatMessageResponse createChatMessage(ChatMessageRequest req, Long senderId) {
        ChatRoom chatRoom = chatRoomRepository.findById(req.getChatRoomId())
                .orElseThrow(()-> new ClientException(ErrorCode.INVALID_CHAT_REQUEST));

        User sender = userRepository.findById(senderId)
                .orElseThrow(()->new ClientException(ErrorCode.USER_NOT_FOUND));
        ChatMessage message = new ChatMessage(chatRoom, senderId, req.getContent());
        chatMessageRepository.save(message);

        ChatMessageResponse response = ChatMessageResponse.fromEntity(message, sender);
        response.setNickname(sender.getNickname());

        return response;
    }
}