package com.fifteen.auction.domain.chat.controller;

import com.fifteen.auction.domain.chat.dto.request.CreateChatRoomRequest;
import com.fifteen.auction.domain.chat.dto.response.ChatRoomResponse;
import com.fifteen.auction.domain.chat.service.ChatRoomService;
import com.fifteen.auction.domain.user.auth.entity.AuthUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChatRoomController {
    private final ChatRoomService chatRoomService;

    @PostMapping("/v1/chatRoom")
    public ResponseEntity<ChatRoomResponse> createChatRoom(@RequestBody CreateChatRoomRequest req,
                                                           @AuthenticationPrincipal AuthUser authUser){
        return new ResponseEntity<>(chatRoomService.createChatRoom(authUser.getId(), req.getSellerId()), HttpStatus.CREATED);
    }
}
