package com.fifteen.auction.domain.chat.controller;

import com.fifteen.auction.domain.chat.dto.request.ChatMessageRequest;
import com.fifteen.auction.domain.chat.dto.response.ChatMessageResponse;
import com.fifteen.auction.domain.chat.service.chat.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/v1/message")
    public void sendMessageV1(ChatMessageRequest req,
                              SimpMessageHeaderAccessor accessor) {
        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        ChatMessageResponse response = chatMessageService.sendChatMessageV1(req, userId);
        messagingTemplate.convertAndSend("/sub/channel/" + req.getChatRoomId(), response);
    }

    @MessageMapping("/v2/message")
    public void sendMessageV2(ChatMessageRequest req,
                              SimpMessageHeaderAccessor accessor) {
        Long userId = (Long) accessor.getSessionAttributes().get("userId");

        chatMessageService.sendChatMessageV2(req,userId);
    }
}
