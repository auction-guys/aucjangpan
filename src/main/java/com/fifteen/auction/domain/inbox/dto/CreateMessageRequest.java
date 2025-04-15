package com.fifteen.auction.domain.inbox.dto;

import com.fifteen.auction.domain.inbox.entity.MessageType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class CreateMessageRequest {

    private Long destUserId;
    private MessageType type;
    private String message;
}
