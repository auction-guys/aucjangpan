package com.fifteen.auction.domain.chat.service.chat;

import com.fifteen.auction.domain.chat.dto.request.ChatMessageRequest;
import com.fifteen.auction.domain.chat.dto.response.ChatMessageResponse;
import com.fifteen.auction.domain.chat.entity.ChatMessage;
import com.fifteen.auction.domain.chat.entity.ChatRoom;
import com.fifteen.auction.domain.chat.repository.message.ChatMessageRepository;
import com.fifteen.auction.domain.chat.repository.room.ChatRoomRepository;
import com.fifteen.auction.domain.chat.service.redis.RedisPublisher;
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
    private final ChatPersistenceService chatPersistenceService;
    private final UserRepository userRepository;
    private final RedisPublisher redisPublisher;


    @Transactional
    public ChatMessageResponse sendChatMessageV1(ChatMessageRequest req, Long senderId) {
        ChatRoom chatRoom = chatRoomRepository.findById(req.getChatRoomId())
                .orElseThrow(()-> new ClientException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        User sender = userRepository.findById(senderId)
                .orElseThrow(()->new ClientException(ErrorCode.USER_NOT_FOUND));
        ChatMessage message = new ChatMessage(chatRoom, senderId, req.getContent());
        chatMessageRepository.save(message);

        ChatMessageResponse response = ChatMessageResponse.fromEntity(message, sender);
        response.setNickname(sender.getNickname());

        return response;
    }

    @Transactional(readOnly = true)
    public void sendChatMessageV2(ChatMessageRequest req, Long senderId) {
        ChatRoom chatRoom = chatRoomRepository.findById(req.getChatRoomId())
                .orElseThrow(()-> new ClientException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        User sender = userRepository.findById(senderId)
                .orElseThrow(()-> new ClientException(ErrorCode.USER_NOT_FOUND));

        ChatMessage chatMessage = new ChatMessage(chatRoom, senderId, req.getContent());
        ChatMessageResponse response = ChatMessageResponse.fromEntity(chatMessage,sender);

        redisPublisher.publishChatMessage(response);
        // 비동기적으로 DB에 저장
        chatPersistenceService.saveMessageAsync(chatMessage);
    }
}