package com.fifteen.auction.domain.chat.controller;

import com.fifteen.auction.domain.auction.dto.response.AuctionLog;
import com.fifteen.auction.domain.chat.dto.request.CreateChatRoomRequest;
import com.fifteen.auction.domain.chat.dto.response.ChatRoomListResponse;
import com.fifteen.auction.domain.chat.dto.response.ChatRoomResponse;
import com.fifteen.auction.domain.chat.service.chat.ChatRoomService;
import com.fifteen.auction.domain.user.auth.entity.AuthUser;
import com.fifteen.auction.global.dto.PageCond;
import com.fifteen.auction.global.dto.PageInfo;
import com.fifteen.auction.global.dto.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/v1/chatRoom")
    public ResponseEntity<Response<List<ChatRoomListResponse>>> findChatRooms(
            @AuthenticationPrincipal AuthUser authUser,
            @ModelAttribute PageCond cond){
        log.info("controller");
        Page<ChatRoomListResponse> list = chatRoomService.findChatRooms(cond, authUser.getId());
        log.info("senvice END");
        return ResponseEntity.ok(Response.of(list.getContent(), PageInfo.fromPage(list)));
    }
}