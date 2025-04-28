package com.fifteen.auction.domain.auction.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fifteen.auction.domain.auction.dto.event.AuctionExpirationEvent;
import com.fifteen.auction.domain.auction.dto.event.BuyNowV2ProcessEvent;
import com.fifteen.auction.domain.auction.service.port.in.AuctionScheduleProcessor;
import com.fifteen.auction.domain.auction.service.port.out.AuctionEndScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;

@Slf4j @Component
@RequiredArgsConstructor
public class AwsAuctionMessageListener {

    private final AuctionScheduleProcessor auctionScheduleProcessor;
    private final AuctionEndScheduleService auctionEndScheduleService;

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
                auctionEndScheduleService.cancelScheduleAuctionEnd(e.getAuctionSeq());
                auctionScheduleProcessor.processExpired(e.getAuctionId(), e.getAuctionSeq(), e.getStartPrice());
            } catch (JsonProcessingException ex) {
                log.info("[JsonProcessingException] m.body = {}", m.body());
                throw new RuntimeException(ex);
            }
        });
    }

    @EventListener(BuyNowV2ProcessEvent.class)
    public void handleBuyNowV2ProcessEvent(BuyNowV2ProcessEvent e) {
        auctionEndScheduleService.cancelScheduleAuctionEnd(e.getAuctionSeq());
        auctionScheduleProcessor.processBuyNowMessage(e.getAuctionSeq(), e.getWinnerId());
    }
}
