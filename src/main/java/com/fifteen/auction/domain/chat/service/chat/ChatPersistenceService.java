package com.fifteen.auction.domain.chat.service.chat;

import com.fifteen.auction.domain.chat.entity.ChatMessage;
import com.fifteen.auction.domain.chat.repository.message.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatPersistenceService {

    private final ChatMessageRepository chatMessageRepository;

    @Async
    @Transactional
    public void saveMessageAsync(ChatMessage chatMessage) {
        try {
            chatMessageRepository.save(chatMessage);
        } catch (Exception e) {
            log.error("Failed to save chat message asynchronously: {}", chatMessage, e);
            // TODO: 비동기 DB 저장 실패시 재시도 등 에러시 로직 고려
        }
    }
}
