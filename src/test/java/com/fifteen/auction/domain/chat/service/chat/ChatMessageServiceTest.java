package com.fifteen.auction.domain.chat.service.chat;

import com.fifteen.auction.domain.chat.dto.request.ChatMessageRequest;
import com.fifteen.auction.domain.chat.dto.response.ChatMessageResponse;
import com.fifteen.auction.domain.chat.entity.ChatMessage;
import com.fifteen.auction.domain.chat.entity.ChatRoom;
import com.fifteen.auction.domain.chat.repository.message.ChatMessageRepository;
import com.fifteen.auction.domain.chat.repository.room.ChatRoomRepository;
import com.fifteen.auction.domain.chat.service.redis.RedisPublisher;
import com.fifteen.auction.domain.recommend.enums.AgeGroup;
import com.fifteen.auction.domain.recommend.enums.Gender;
import com.fifteen.auction.domain.recommend.enums.Region;
import com.fifteen.auction.domain.user.entity.User;
import com.fifteen.auction.domain.user.enums.UserRole;
import com.fifteen.auction.domain.user.repository.UserRepository;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;
    @Mock
    private ChatRoomRepository chatRoomRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RedisPublisher redisPublisher;
    @Mock
    private ChatPersistenceService chatPersistenceService;
    @InjectMocks
    private ChatMessageService chatMessageService;

    @Nested
    class SaveChatMessageV1Tests{
        @Test
        void 메시지_전송_요청_성공_V1() {
            // given
            Long senderId = 1L;
            Long chatRoomId = 100L;
            String content = "안녕하세요";

            ChatMessageRequest request = new ChatMessageRequest(content, chatRoomId);
            ChatRoom chatRoom = new ChatRoom();
            User sender = new User(
                    "test@email.com",
                    "TestUser",
                    "홍길동",
                    Gender.MALE,
                    AgeGroup.TWENTIES,
                    "password123",
                    Region.SEOUL,
                    "010-1234-5678",
                    "IT/전자기기",
                    "123-456-789",
                    null,
                    UserRole.ROLE_USER
            );

            when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom));
            when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));

            // when
            ChatMessageResponse response = chatMessageService.sendChatMessageV1(request, senderId);

            // then
            verify(chatRoomRepository).findById(chatRoomId);
            verify(userRepository).findById(senderId);

            assertThat(response).isNotNull();
            assertThat(response.getNickname()).isEqualTo("TestUser");
            assertThat(response.getContent()).isEqualTo(content);
        }

        @Test
        void 메시지_전송_요청_존재하지_않는_채팅방_V1() {
            // given
            Long senderId = 1L;
            Long chatRoomId = 100L;
            String content ="안녕하세요";

            ChatMessageRequest request = new ChatMessageRequest(content, chatRoomId);

            when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.empty());

            // when & then
            ClientException exception = assertThrows(ClientException.class, () -> {
                chatMessageService.sendChatMessageV1(request, senderId);
            });

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CHAT_ROOM_NOT_FOUND);
        }

        @Test
        void 메시지_전송_요청_존재하지_않는_유저_V1() {
            // given
            Long senderId = 1L;
            Long chatRoomId = 100L;
            String content = "안녕하세요";

            ChatMessageRequest request = new ChatMessageRequest(content, chatRoomId);
            ChatRoom chatRoom = new ChatRoom();

            when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom));
            when(userRepository.findById(senderId)).thenReturn(Optional.empty());

            // when & then
            ClientException exception = assertThrows(ClientException.class, () -> {
                chatMessageService.sendChatMessageV1(request, senderId);
            });

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    class SaveChatMessageV2Tests{
        @Test
        void 메시지_전송_요청_성공_V2(){
            // given
            Long senderId = 1L;
            Long chatRoomId = 100L;
            String content = "안녕하세요";

            ChatMessageRequest request = new ChatMessageRequest(content,chatRoomId);
            ChatRoom chatRoom = new ChatRoom();
            User sender = new User();

            when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom));
            when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));

            // when
            chatMessageService.sendChatMessageV2(request, senderId);

            // then
            verify(redisPublisher).publishChatMessage(any(ChatMessageResponse.class));
            verify(chatPersistenceService).saveMessageAsync(any(ChatMessage.class));
        }

        @Test
        void 메시지_전송_요청_존재하지_않는_채팅방_V2(){
            // given
            Long senderId = 1L;
            Long chatRoomId = 100L;
            String content = "안녕하세요";

            ChatMessageRequest request = new ChatMessageRequest(content, chatRoomId);

            when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.empty());

            // when & then
            ClientException exception = assertThrows(ClientException.class, () -> {
                chatMessageService.sendChatMessageV2(request, senderId);
            });

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CHAT_ROOM_NOT_FOUND);
        }
        @Test
        void 메시지_전송_요청_존재하지_않는_유저_V2(){
            // given
            Long senderId = 1L;
            Long chatRoomId = 100L;
            String content = "안녕하세요";

            ChatMessageRequest request = new ChatMessageRequest(content, chatRoomId);
            ChatRoom chatRoom = new ChatRoom();

            when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom));
            when(userRepository.findById(senderId)).thenReturn(Optional.empty());

            // when & then
            ClientException exception = assertThrows(ClientException.class, () -> {
                chatMessageService.sendChatMessageV2(request, senderId);
            });

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        }
    }
}