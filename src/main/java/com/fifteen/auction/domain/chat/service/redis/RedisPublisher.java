package com.fifteen.auction.domain.chat.service.redis;

import com.fifteen.auction.domain.chat.dto.response.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisPublisher {
    private final RedisTemplate<String, Object> chatMessageRedisTemplate;

    /**
     * Redis 채널로 메시지 발행
     *
     * @param channelName 발행할 채널 이름 (예: "chat:room:123")
     * @param response     발행할 ChatMessageResponse 객체
     */
    public void publish(String channelName, ChatMessageResponse response) {
        // objectRedisTemplate을 사용하여 메시지 발행 (Jackson2JsonRedisSerializer가 객체를 JSON으로 변환)
        chatMessageRedisTemplate.convertAndSend(channelName, response);
    }

    /**
     * 채팅 메시지를 받아 해당 채팅방의 Redis 채널로 발행
     * 채널 이름 형식: 'chat:room:{roomId}'
     *
     * @param response 발행할 ChatMessageResponse 객체
     */
    public void publishChatMessage(ChatMessageResponse response) {
        if (response == null || response.getChatRoomId() == null) {
            return;
        }
        String channelName = "chat:room:" + response.getChatRoomId();
        publish(channelName, response);
    }
}
