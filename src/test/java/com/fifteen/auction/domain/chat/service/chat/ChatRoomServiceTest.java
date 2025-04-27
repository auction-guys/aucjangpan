package com.fifteen.auction.domain.chat.service.chat;

import com.fifteen.auction.domain.chat.dto.response.ChatMessageResponse;
import com.fifteen.auction.domain.chat.dto.response.ChatRoomListResponse;
import com.fifteen.auction.domain.chat.dto.response.ChatRoomResponse;
import com.fifteen.auction.domain.chat.entity.ChatRoom;
import com.fifteen.auction.domain.chat.repository.message.ChatMessageRepository;
import com.fifteen.auction.domain.chat.repository.room.ChatRoomRepository;
import com.fifteen.auction.domain.recommend.enums.AgeGroup;
import com.fifteen.auction.domain.recommend.enums.Gender;
import com.fifteen.auction.domain.recommend.enums.Region;
import com.fifteen.auction.domain.user.entity.User;
import com.fifteen.auction.domain.user.enums.UserRole;
import com.fifteen.auction.domain.user.repository.UserRepository;
import com.fifteen.auction.global.dto.PageCond;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ChatRoomRepository chatRoomRepository;
    @Mock
    private ChatMessageRepository chatMessageRepository;
    @InjectMocks
    private ChatRoomService chatRoomService;

    @Nested
    class createChatRoomTests {
        @Test
        void 채팅방_존재시_채팅방정보와_메시지목록을_반환한다() {
            Long senderId = 1L;
            Long userId = 2L;
            Long chatRoomId = 1L;
            String content = "안녕하세요";
            LocalDateTime createdAt = LocalDateTime.now();
            User sender = new User(
                    "sender@email.com",
                    "sender",
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

            User user = new User(
                    "user@email.com",
                    "user",
                    "놀부",
                    Gender.MALE,
                    AgeGroup.TWENTIES,
                    "password123",
                    Region.SEOUL,
                    "010-5678-1234",
                    "IT/전자기기",
                    "123-789-456",
                    null,
                    UserRole.ROLE_USER
            );

            ChatRoom chatRoom = new ChatRoom(senderId, userId);
            ReflectionTestUtils.setField(chatRoom, "id", chatRoomId);

            ChatMessageResponse message = new ChatMessageResponse(senderId, sender.getNickname(),content, chatRoomId, createdAt);
            List<ChatMessageResponse> messageList = List.of(message);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(chatRoomRepository.findByUserIdAndSellerId(senderId, userId)).thenReturn(Optional.of(chatRoom));
            when(chatMessageRepository.findMessagesWithUserByChatRoom(chatRoom)).thenReturn(messageList);

            ChatRoomResponse response = chatRoomService.createChatRoom(senderId, userId);

            assertNotNull(response);
            assertEquals(chatRoom.getId(), response.getChatRoomId());
            assertEquals(messageList, response.getMessages());

            verify(userRepository, times(1)).findById(userId);
            verify(chatRoomRepository, times(1)).findByUserIdAndSellerId(senderId, userId);
            verify(chatMessageRepository, times(1)).findMessagesWithUserByChatRoom(chatRoom);
            verify(chatRoomRepository, never()).save(any());
        }

        @Test
        void 채팅방이_없을시_채팅방_생성_후_반환한다() {
            Long senderId = 1L;
            Long userId = 2L;
            Long chatRoomId = 1L;

            User user = new User(
                    "user@email.com",
                    "user",
                    "놀부",
                    Gender.MALE,
                    AgeGroup.TWENTIES,
                    "password123",
                    Region.SEOUL,
                    "010-5678-1234",
                    "IT/전자기기",
                    "123-789-456",
                    null,
                    UserRole.ROLE_USER
            );

            ChatRoom chatRoom = new ChatRoom(senderId, userId);
            ReflectionTestUtils.setField(chatRoom, "id", chatRoomId);

            // Mock 설정
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(chatRoomRepository.findByUserIdAndSellerId(senderId, userId)).thenReturn(Optional.empty());
            when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(chatRoom);

            ChatRoomResponse response = chatRoomService.createChatRoom(senderId, userId);

            assertNotNull(response);
            assertEquals(chatRoom.getId(), response.getChatRoomId());
            assertTrue(response.getMessages().isEmpty());

            verify(chatMessageRepository, never()).findMessagesWithUserByChatRoom(any());
        }

        @Test
        void 요청자ID와_수신자ID가_같을시_에러가_발생한다() {
            Long senderId = 1L;

            ClientException exception = assertThrows(ClientException.class, () -> {
                chatRoomService.createChatRoom(senderId, senderId);
            });
            assertEquals(ErrorCode.INVALID_CHAT_REQUEST, exception.getErrorCode());

            verify(userRepository, never()).findById(anyLong());
            verify(chatRoomRepository, never()).findByUserIdAndSellerId(anyLong(), anyLong());
            verify(chatRoomRepository, never()).save(any());
            verify(chatMessageRepository, never()).findMessagesWithUserByChatRoom(any());
        }

        @Test
        void 수신자ID가_존재하지_않을시_에러가_발생한다() {
            Long senderId= 1L;
            Long userId = 2L;
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            ClientException exception = assertThrows(ClientException.class, () -> {
                chatRoomService.createChatRoom(senderId, userId);
            });
            assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());

            verify(chatRoomRepository, never()).findByUserIdAndSellerId(anyLong(), anyLong());
            verify(chatRoomRepository, never()).save(any());
            verify(chatMessageRepository, never()).findMessagesWithUserByChatRoom(any());
        }


    }

    @Nested
    class findChatRoomsTests {
        @Test
        void 채팅방_목록을_반환한다() {
            PageCond cond = new PageCond(1, 5);
            Long userId = 1L;
            Pageable expectedPageable = PageRequest.of(0, 5);
            List<ChatRoomListResponse> mockList = List.of(new ChatRoomListResponse(/*...*/));
            Page<ChatRoomListResponse> mockPage = new PageImpl<>(mockList, expectedPageable, mockList.size());

            when(chatRoomRepository.findChatRoomsByUserId(eq(userId), any(Pageable.class)))
                    .thenReturn(mockPage);

            Page<ChatRoomListResponse> resultPage = chatRoomService.findChatRooms(cond, userId);

            assertNotNull(resultPage);
            assertEquals(mockPage, resultPage);
        }
    }
}