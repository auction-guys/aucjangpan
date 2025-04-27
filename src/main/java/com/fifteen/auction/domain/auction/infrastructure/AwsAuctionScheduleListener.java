package com.fifteen.auction.domain.auction.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fifteen.auction.domain.auction.dto.event.AuctionExpirationEvent;
import com.fifteen.auction.domain.auction.repository.auction.AuctionRedisRepository;
import com.fifteen.auction.domain.auction.service.AuctionService;
import com.fifteen.auction.domain.auction.service.port.in.AuctionScheduleUseCase;
import com.fifteen.auction.domain.inbox.dto.CreateMessageRequest;
import com.fifteen.auction.domain.inbox.service.InboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;

@Slf4j @Service
@RequiredArgsConstructor
public class AwsAuctionScheduleListener implements AuctionScheduleUseCase {

    private final InboxService inboxService;
    private final AuctionService auctionService;

    private final AuctionRedisRepository auctionRedisRepository;

    private final SqsClient sqsClient;
    private final ObjectMapper om = new ObjectMapper();

    @Value("${aws-scheduler.url.sqs}")
    private String sqsUrl;

    @Scheduled(fixedDelay = 5000)
    public void pollExpirationMessages() {
        ReceiveMessageRequest req = ReceiveMessageRequest.builder()
                .queueUrl(sqsUrl)
                .waitTimeSeconds(20)
                .maxNumberOfMessages(10)
                .build();

        List<Message> messages = sqsClient.receiveMessage(req).messages();

        messages.forEach(m -> {
            try {
                sqsClient.deleteMessage(b -> b.queueUrl(sqsUrl).receiptHandle(m.receiptHandle()));
                AuctionExpirationEvent e = om.readValue(m.body(), AuctionExpirationEvent.class);
                processExpired(e.getAuctionId(), e.getAuctionSeq(), e.getStartPrice());
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Override
    public void processExpired(Long auctionId, String auctionSeq, Long startPrice) {
        // 현재 가격 가져오기
        Long currentPrice = auctionRedisRepository.findCurrentPrice(auctionSeq);

        // 입찰 이력 긁어오기
        List<Long> participants = auctionRedisRepository.findParticipants(auctionSeq);

        // 유찰 처리
        if (currentPrice.equals(startPrice)) {
            auctionService.miscarry(auctionId);
            sendParticipantsMessage(auctionSeq, participants);
            return;
        }

        // 낙찰자 처리
        Long winnerId = participants.get(0);
        auctionService.processWinning(auctionSeq, winnerId, currentPrice);
        sendWinnerMessage(auctionSeq, winnerId);

        // 이외 사람들 처리
        sendParticipantsMessage(auctionSeq, participants.subList(1, participants.size()));

        auctionRedisRepository.flushTopBidCache(auctionSeq);

        log.info("[경매 알림 전송 완료] auctionSeq={}", auctionSeq);
    }

    @Override
    public void cancelReservedEvent(String auctionSeq) {

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
