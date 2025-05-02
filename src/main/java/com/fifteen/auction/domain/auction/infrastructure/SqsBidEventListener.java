package com.fifteen.auction.domain.auction.infrastructure;

import com.fifteen.auction.domain.auction.dto.event.BidRequestEvent;
import com.fifteen.auction.domain.auction.dto.event.BuyNowRequestEvent;
import com.fifteen.auction.domain.auction.service.port.in.BidEventHandler;
import com.fifteen.auction.domain.auction.util.JsonUtil;
import com.fifteen.auction.global.dto.exception.ClientException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;

import static com.fifteen.auction.global.config.QueueConfig.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SqsBidEventListener {

    private final BidEventHandler bidEventHandler;

    @Value("${cloud.aws.sqs.url.auction-req-queue-fifo}")
    private String sqsUrl;

    private final SqsClient sqsClient;

    private Thread listenerThread;

    private void auctionParticipationHandler() {
        while (!listenerThread.isInterrupted()) {
            ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                    .queueUrl(sqsUrl)
                    .waitTimeSeconds(20)
                    .maxNumberOfMessages(10)
                    .messageAttributeNames("All")
                    .build();

            List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).messages();

            messages.forEach(m -> {
                String typeId = m.messageAttributes().get(TYPE_ID_HEADER_KEY).stringValue();
                String bodyString = m.body();
                String receiptHandle = m.receiptHandle();
                processEvent(typeId, bodyString, receiptHandle);
            });
        }
    }

    private void processEvent(String typeId, String bodyString, String receiptHandle) {
        try {
            switch (typeId) {
                case TYPE_ID_BID_REQUEST_EVENT -> {
                    BidRequestEvent e = JsonUtil.readValue(bodyString, BidRequestEvent.class);
                    log.info("[입찰 큐 이벤트 처리] auctionSeq={}\tbidderId={}\tbidPrice={}", e.getAuctionSeq(), e.getUserId(), e.getBidPrice());
                    bidEventHandler.handleBidFromQueue(e.getAuctionSeq(), e.getUserId(), e.getBidPrice());
                }
                case TYPE_ID_BUY_NOW_REQUEST_EVENT -> {
                    BuyNowRequestEvent e = JsonUtil.readValue(bodyString, BuyNowRequestEvent.class);
                    log.info("[즉시 구매 큐 이벤트 처리] auctionSeq={}\tbidderId={}\tbidPrice={}", e.getAuctionSeq(), e.getUserId(), e.getBidPrice());
                    bidEventHandler.handleBuyNowFromQueue(e.getAuctionSeq(), e.getUserId());
                }
                default -> log.warn("[RabbitBidRequestListener] 알 수 없는 이벤트 타입={}", typeId);
            }
            sqsClient.deleteMessage(b -> b.queueUrl(sqsUrl).receiptHandle(receiptHandle));
        } catch (ClientException e) {
            sqsClient.deleteMessage(b -> b.queueUrl(sqsUrl).receiptHandle(receiptHandle));

            // TODO(yeonic): 여기에 최종 결과를 업데이트 하는 로직 추가 예정
            log.info("[SQS Listener] [{}] message={}", e.getErrorCode().name(), e.getErrorCode().getMessage());
        }
    }


    @PostConstruct
    public void initAuctionListener() {
        startListener();
    }

    @PreDestroy
    public void stop() {
        listenerThread.interrupt();
        log.info("[SQS Listener] Gracefully Shutdown.");
    }

    private void startListener() {
        listenerThread = new Thread(() -> {
            try {
                auctionParticipationHandler();
            } catch (Exception e) {
                log.warn("[SQS Listener] Listener Thread가 비정상 종료되었습니다. 재시작합니다.");
                if (!listenerThread.isInterrupted()) {
                    startListener();
                }
            }
        });
        listenerThread.start();
    }

}
