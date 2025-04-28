package com.fifteen.auction.domain.auction.service;

import com.fifteen.auction.domain.auction.repository.auction.AuctionRedisRepository;
import com.fifteen.auction.domain.auction.service.port.in.AuctionScheduleProcessor;
import com.fifteen.auction.domain.inbox.dto.CreateMessageRequest;
import com.fifteen.auction.domain.inbox.service.InboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j @Service
@RequiredArgsConstructor
public class AuctionScheduleService implements AuctionScheduleProcessor {

    private final AuctionRedisRepository auctionRedisRepository;
    private final AuctionService auctionService;
    private final InboxService inboxService;

    @Override
    public void processExpired(Long auctionId, String auctionSeq, Long startPrice) {
        // 현재 가격 가져오기
        Long currentPrice = auctionRedisRepository.findCurrentPrice(auctionSeq);

        // 유찰 처리
        if (currentPrice.equals(startPrice)) {
            auctionService.miscarry(auctionId);
            return;
        }

        // 입찰 이력 긁어오기
        List<Long> participants = auctionRedisRepository.findParticipants(auctionSeq);

        // 낙찰자 처리
        Long winnerId = participants.get(0);
        auctionService.processWinning(auctionSeq, winnerId, currentPrice);
        sendWinnerMessage(auctionSeq, winnerId);

        // 이외 사람들 처리
        sendParticipantsMessage(auctionSeq, participants.subList(1, participants.size()));

        auctionRedisRepository.flushTopBidCache(auctionSeq);

        log.info("[경매 종료 알림 전송 완료] auctionSeq={}", auctionSeq);
    }

    @Override
    public void processBuyNowMessage(String auctionSeq, Long winnerId) {
        // 낙찰자 메시지 전송
        sendWinnerMessage(auctionSeq, winnerId);

        // 참가자 메시지 전송 처리
        List<Long> participants = auctionRedisRepository.findParticipants(auctionSeq);
        sendParticipantsMessage(auctionSeq, participants);

        auctionRedisRepository.flushTopBidCache(auctionSeq);

        log.info("[즉시 구매 알림 전송 완료] auctionSeq={}", auctionSeq);
    }

    private void sendParticipantsMessage(String auctionSeq, List<Long> participants) {
        List<CreateMessageRequest> messages = participants.stream()
                .map(userId -> CreateMessageRequest.forParticipants(userId, auctionSeq))
                .toList();
        inboxService.addMultipleMessages(messages);
    }

    private void sendWinnerMessage(String auctionSeq, Long winnerId) {
        CreateMessageRequest winnerMessage = CreateMessageRequest.forWinner(winnerId, auctionSeq);
        inboxService.addSingleMessage(winnerMessage);
    }
}
