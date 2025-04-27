package com.fifteen.auction.domain.chat.service.redis;

import com.fifteen.auction.domain.chat.dto.response.ChatMessageResponse;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisPublisherTest {

    @Mock
    private RedisTemplate<String, Object> chatMessageRedisTemplate;

    @InjectMocks
    private RedisPublisher redisPublisher;

    @Nested
    class publishChatMessageTests {
        @Test
        void 요청을_받으면_올바른_채널명으로_RedisTemplate를_호출한다() {
            Long chatRoomId = 3L;
            ChatMessageResponse response = new ChatMessageResponse(1L, "sender", "안녕하세요", 3L, LocalDateTime.now());
            String expectedChannelName = "chat:room:" + chatRoomId;

            ArgumentCaptor<String> channelCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<ChatMessageResponse> responseCaptor = ArgumentCaptor.forClass(ChatMessageResponse.class);

            redisPublisher.publishChatMessage(response);

            verify(chatMessageRedisTemplate, times(1))
                    .convertAndSend(channelCaptor.capture(), responseCaptor.capture());

            assertEquals(expectedChannelName, channelCaptor.getValue());
            assertEquals(response, responseCaptor.getValue());
        }
    }
        @Test
        void 응답객체가_NULL이면_RedisTemplate를_호출하지_않는다() {

            ChatMessageResponse nullResponse = null;

            redisPublisher.publishChatMessage(nullResponse);

            verify(chatMessageRedisTemplate, never()).convertAndSend(anyString(), any());
        }

        @Test
        void 채팅방ID가_NULL이면_RedisTemplate를_호출하지_않는다() {
            ChatMessageResponse response = new ChatMessageResponse(1L, "sender", "안녕하세요", null, LocalDateTime.now());

            redisPublisher.publishChatMessage(response);

            verify(chatMessageRedisTemplate, never()).convertAndSend(anyString(), any());
        }
    }
