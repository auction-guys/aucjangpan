package com.fifteen.auction.domain.auction.service;

import com.fifteen.auction.domain.auction.dto.event.AuctionOpenEvent;
import com.fifteen.auction.domain.inbox.dto.CreateMessageRequest;
import com.fifteen.auction.domain.inbox.service.InboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

@Slf4j @Service
@RequiredArgsConstructor
public class ScheduledAuctionService {
    private final TaskScheduler taskScheduler;

    private final AuctionCacheService auctionCacheService;
    private final AuctionService auctionService;
    private final InboxService inboxService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void registerAuctionExpiration(AuctionOpenEvent event) {
        // 스케줄러 세팅
        Instant reservedTime = event.getExpiresAt().atZone(ZoneId.systemDefault()).toInstant();
        taskScheduler.schedule(
                sendExpirationMessage(event.getAuctionId(), event.getAuctionSeq(), event.getStartPrice()),
                reservedTime
        );
    }

    private Runnable sendExpirationMessage(Long auctionId, String auctionSeq, Long startPrice) {
        return () -> {
            // 현재 가격 가져오기
            Long currentPrice = auctionCacheService.findCurrentPrice(auctionSeq);

            // 유찰 처리
            if (currentPrice.equals(startPrice)) {
                auctionService.miscarry(auctionId);
                return;
            }

            // 입찰 이력 긁어오기
            List<Long> participants = auctionCacheService.findParticipants(auctionSeq);

            // 낙찰자 처리
            Long winnerId = participants.get(0);
            auctionService.processWinning(auctionSeq, winnerId, currentPrice);
            sendWinnerMessage(auctionSeq, winnerId);

            // 이외 사람들 처리
            sendParticipantsMessage(auctionSeq, participants);

            auctionCacheService.flushTopBidCache(auctionSeq);

            log.info("sent expiration message for auction: {}", auctionSeq);
        };
    }

    private void sendParticipantsMessage(String auctionSeq, List<Long> participants) {
        List<CreateMessageRequest> messages = participants.subList(1, participants.size()).stream()
                .map(userId -> CreateMessageRequest.forParticipants(userId, auctionSeq))
                .toList();
        inboxService.addMultipleMessages(messages);
    }

    private void sendWinnerMessage(String auctionSeq, Long winnerId) {
        CreateMessageRequest winnerMessage = CreateMessageRequest.forWinner(winnerId, auctionSeq);
        inboxService.addSingleMessage(winnerMessage);
    }
}
