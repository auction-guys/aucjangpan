package com.fifteen.auction.domain.auction.infrastructure;

import com.fifteen.auction.domain.auction.dto.event.BidRequestEvent;
import com.fifteen.auction.domain.auction.dto.event.BuyNowRequestEvent;
import com.fifteen.auction.domain.auction.service.port.out.AuctionEventPublisher;
import com.fifteen.auction.domain.auction.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.Map;

import static com.fifteen.auction.global.config.QueueConfig.*;

@Slf4j @Component
@RequiredArgsConstructor
public class SqsBidEventPublisher implements AuctionEventPublisher {

    @Value("${cloud.aws.sqs.url.auction-req-queue-fifo}")
    private String sqsUrl;

    private final SqsClient sqsClient;

    public static final String STRING_DATA_TYPE = "String";

    @Override
    public void publishBidRequest(BidRequestEvent event) {
        SendMessageRequest sendRequest = SendMessageRequest.builder()
                .queueUrl(sqsUrl)
                .messageAttributes(
                        Map.of(TYPE_ID_HEADER_KEY, MessageAttributeValue.builder()
                                .stringValue(TYPE_ID_BID_REQUEST_EVENT)
                                .dataType(STRING_DATA_TYPE).build())
                )
                .messageBody(JsonUtil.writeValueAsString(event))
                .messageGroupId(event.getAuctionSeq())
                .build();

        sqsClient.sendMessage(sendRequest);

        log.info("[입찰 큐 이벤트 발행] auctionSeq={}\tbidderId={}\tbidPrice={}",
                event.getAuctionSeq(), event.getUserId(), event.getBidPrice());
    }

    @Override
    public void publishBuyNowRequest(BuyNowRequestEvent event) {
        SendMessageRequest sendRequest = SendMessageRequest.builder()
                .queueUrl(sqsUrl)
                .messageAttributes(
                        Map.of(TYPE_ID_HEADER_KEY, MessageAttributeValue.builder()
                                .stringValue(TYPE_ID_BUY_NOW_REQUEST_EVENT)
                                .dataType(STRING_DATA_TYPE).build())
                )
                .messageBody(JsonUtil.writeValueAsString(event))
                .messageGroupId(event.getAuctionSeq())
                .build();

        sqsClient.sendMessage(sendRequest);

        log.info("[즉시 구매 큐 이벤트 발행] auctionSeq={}\tbidderId={}",
                event.getAuctionSeq(), event.getUserId()
        );
    }
}
