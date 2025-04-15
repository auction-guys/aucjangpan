package com.fifteen.auction.domain.chat.service;

import com.fifteen.auction.domain.chat.dto.request.CreateChatRoomRequest;
import com.fifteen.auction.domain.chat.dto.response.CreateChatRoomResponse;
import com.fifteen.auction.domain.chat.entity.ChatMessage;
import com.fifteen.auction.domain.chat.entity.ChatRoom;
import com.fifteen.auction.domain.chat.repository.ChatRoomRepository;
import com.fifteen.auction.domain.user.entity.User;
import com.fifteen.auction.domain.user.repository.UserRepository;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Transactional
    public CreateChatRoomResponse createChatRoom(Long userId, Long sellerId) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(()-> new ClientException(ErrorCode.USER_NOT_FOUND));

        Optional<ChatRoom> chatRoom = chatRoomRepository.findByUserIdAndSellerId(userId, sellerId);

        if(chatRoom.isPresent()){
            return CreateChatRoomResponse.fromEntity(chatRoom.get());
        }

        return CreateChatRoomResponse.fromEntity(
                chatRoomRepository.save(new ChatRoom(userId, sellerId))
        );
    }
}
