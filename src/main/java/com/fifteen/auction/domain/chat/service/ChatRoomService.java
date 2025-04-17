package com.fifteen.auction.domain.chat.service;

import com.fifteen.auction.domain.chat.dto.response.ChatMessageResponse;
import com.fifteen.auction.domain.chat.dto.response.ChatRoomResponse;
import com.fifteen.auction.domain.chat.entity.ChatRoom;
import com.fifteen.auction.domain.chat.repository.message.ChatMessageRepository;
import com.fifteen.auction.domain.chat.repository.room.ChatRoomRepository;
import com.fifteen.auction.domain.user.repository.UserRepository;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public ChatRoomResponse createChatRoom(Long userId, Long sellerId) {
        if (userId.equals(sellerId)) {
            throw new ClientException(ErrorCode.INVALID_CHAT_REQUEST);
        }

        userRepository.findById(sellerId).orElseThrow(()-> new ClientException(ErrorCode.USER_NOT_FOUND));
        Optional<ChatRoom> chatRoom = chatRoomRepository.findByUserIdAndSellerId(userId, sellerId);

        if (chatRoom.isPresent()) {
            List<ChatMessageResponse> messages = chatMessageRepository.findMessagesWithUserByChatRoom(chatRoom.get());
            return ChatRoomResponse.fromEntity(chatRoom.get(), messages);
        }

        ChatRoom newChatRoom = chatRoomRepository.save(new ChatRoom(userId, sellerId));
        return ChatRoomResponse.fromEntity(newChatRoom, Collections.emptyList());
    }
}
