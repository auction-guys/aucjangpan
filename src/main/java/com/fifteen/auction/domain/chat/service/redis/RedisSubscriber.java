package com.fifteen.auction.domain.chat.service.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fifteen.auction.domain.chat.dto.response.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSubscriber {
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    public void handleMessage(String publishedMessage) {
        try {
            ChatMessageResponse response = objectMapper.readValue(publishedMessage, ChatMessageResponse.class);

            if (response == null || response.getChatRoomId() == null) {
                log.error("Received invalid message structure or null roomId: {}", publishedMessage);
                return;
            }

            // 해당 채팅방을 구독 중인 클라이언트들에게 메시지 전송
            String destination = "/sub/channel/" + response.getChatRoomId();
            messagingTemplate.convertAndSend(destination, response);

        } catch (Exception e) {
            log.error("Error processing message from Redis: {}", publishedMessage, e);
        }
    }
}
