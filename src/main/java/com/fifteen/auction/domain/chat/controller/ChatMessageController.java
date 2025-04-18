package com.fifteen.auction.domain.chat.controller;

import com.fifteen.auction.domain.chat.dto.request.ChatMessageRequest;
import com.fifteen.auction.domain.chat.dto.response.ChatMessageResponse;
import com.fifteen.auction.domain.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;
@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/message")
    public void sendMessage(ChatMessageRequest req,
                            SimpMessageHeaderAccessor accessor){
        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        ChatMessageResponse response = chatMessageService.createChatMessage(req,userId);
        messagingTemplate.convertAndSend("/sub/channel/"+req.getChatRoomId(), response);
    }
}
