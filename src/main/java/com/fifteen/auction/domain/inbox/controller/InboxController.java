package com.fifteen.auction.domain.inbox.controller;

import com.fifteen.auction.domain.inbox.dto.MessageInfo;
import com.fifteen.auction.domain.inbox.service.InboxService;
import com.fifteen.auction.global.dto.PageCond;
import com.fifteen.auction.global.dto.PageInfo;
import com.fifteen.auction.global.dto.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api")
@RequiredArgsConstructor
@RestController
public class InboxController {

    private final InboxService inboxService;

    // TODO(yeonic): authUser
    @GetMapping("/v1/inbox")
    public ResponseEntity<Response<List<MessageInfo>>> allMessages(
            @RequestParam("userId") Long userId,
            @ModelAttribute PageCond pageCond
    ) {
        Page<MessageInfo> result = inboxService.allMessages(userId, pageCond);
        PageInfo pageInfo = PageInfo.builder()
                .pageNum(result.getNumber())
                .pageSize(result.getSize())
                .totalPage(result.getTotalPages())
                .totalElement(result.getTotalElements())
                .build();

        return ResponseEntity.ok(Response.of(result.getContent(), pageInfo));
    }

    // TODO(yeonic): authUser
    @DeleteMapping("/v1/inbox/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable("messageId") Long messageId,
            @RequestParam("userId") Long userId
    ) {
        inboxService.deleteMessage(userId, messageId);

        return ResponseEntity.noContent().build();
    }
}
