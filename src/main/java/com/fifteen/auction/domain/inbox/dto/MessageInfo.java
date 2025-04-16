package com.fifteen.auction.domain.inbox.dto;

import com.fifteen.auction.domain.inbox.entity.Inbox;
import com.fifteen.auction.domain.inbox.entity.MessageType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageInfo {
    private Long messageId;
    private MessageType type;
    private String message;

    public static MessageInfo fromInbox(Inbox inbox) {
        return new MessageInfo(inbox.getId(), inbox.getType(), inbox.getMessage());
    }
}
