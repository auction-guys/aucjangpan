package com.fifteen.auction.domain.inbox.service;

import com.fifteen.auction.domain.inbox.dto.MessageInfo;
import com.fifteen.auction.domain.inbox.entity.Inbox;
import com.fifteen.auction.domain.inbox.repository.InboxRepository;
import com.fifteen.auction.global.dto.PageCond;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InboxService {
    private final InboxRepository inboxRepository;

    public Page<MessageInfo> allMessages(Long userId, PageCond pageCond) {
        Pageable pageable = PageRequest.of(pageCond.getPageNum() - 1, pageCond.getPageSize());

        Page<Inbox> allMessages = inboxRepository.findAllByUserId(pageable, userId);

        List<MessageInfo> messageInfos = allMessages.getContent()
                .stream()
                .map(MessageInfo::fromInbox)
                .toList();

        return new PageImpl<>(messageInfos, pageable, allMessages.getTotalElements());
    }

    public void deleteMessage(Long userId, Long messageId) {
        Inbox findMessage = inboxRepository.findOneByMsgIdAndUserId(messageId, userId)
                .orElseThrow(() -> new ClientException(ErrorCode.INBOX_MSG_NOT_FOUND));

        inboxRepository.delete(findMessage);
    }
}
