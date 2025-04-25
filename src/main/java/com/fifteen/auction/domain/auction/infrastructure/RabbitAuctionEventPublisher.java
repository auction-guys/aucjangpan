package com.fifteen.auction.domain.auction.infrastructure;

import com.fifteen.auction.domain.auction.dto.event.BidRequestEvent;
import com.fifteen.auction.domain.auction.service.port.out.AuctionEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static com.fifteen.auction.global.config.RabbitConfig.BID_EXCHANGE;
import static com.fifteen.auction.global.config.RabbitConfig.BID_ROUTING_KEY_PREFIX;

@Component
@RequiredArgsConstructor
public class RabbitAuctionEventPublisher implements AuctionEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final QueueKeyResolver queueKeyResolver;

    @Override
    public void publishBidRequest(BidRequestEvent event) {
        int queueIndex = queueKeyResolver.fetchKey(event.getAuctionSeq());
        String routingKey = BID_ROUTING_KEY_PREFIX + queueIndex;
        rabbitTemplate.convertAndSend(BID_EXCHANGE, routingKey, event);
    }
}
