package com.fifteen.auction.domain.inbox.repository;

import com.fifteen.auction.domain.inbox.dto.CreateMessageRequest;

import java.util.List;

public interface InboxBulkRepository {
    void sendAllMessages(List<CreateMessageRequest> messages);
}
