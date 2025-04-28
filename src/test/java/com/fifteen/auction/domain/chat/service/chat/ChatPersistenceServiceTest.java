package com.fifteen.auction.domain.chat.service.chat;

import com.fifteen.auction.domain.chat.entity.ChatMessage;
import com.fifteen.auction.domain.chat.repository.message.ChatMessageRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatPersistenceServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @InjectMocks
    private ChatPersistenceService chatPersistenceService;

    @Nested
    class saveMessageAsyncTests{
        @Test
        void 메시지_비동기_DB저장_성공(){
            ChatMessage message = new ChatMessage();
            chatPersistenceService.saveMessageAsync(message);
            verify(chatMessageRepository, times(1)).save(message);
        }
        @Test
        void 메시지_비동기_DB저장_실패(){
            ChatMessage message = new ChatMessage();
            DataAccessException dbException = new DataAccessException("DB 연결오류 발생") {};
            when(chatMessageRepository.save(message)).thenThrow(dbException);
            assertDoesNotThrow(() -> {
                chatPersistenceService.saveMessageAsync(message);
            });

            verify(chatMessageRepository, times(1)).save(message);
        }
    }
}