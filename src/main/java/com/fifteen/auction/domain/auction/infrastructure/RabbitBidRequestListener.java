package com.fifteen.auction.domain.auction.infrastructure;

import com.fifteen.auction.domain.auction.dto.event.BidRequestEvent;
import com.fifteen.auction.domain.auction.dto.event.BuyNowRequestEvent;
import com.fifteen.auction.domain.auction.service.port.in.BidEventHandler;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.fifteen.auction.global.config.RabbitConfig.*;

@Slf4j @Component
@RequiredArgsConstructor
public class RabbitBidRequestListener {

    private final BidEventHandler bidEventHandler;

    private final Jackson2JsonMessageConverter messageConverter;

    @RabbitListener(queues = "#{rabbitConfig.queueNames}")
    public void auctionParticipationListener(Message message, Channel channel) throws IOException {
        String typeId = message.getMessageProperties().getHeader(TYPE_ID_HEADER_KEY);
        Object payload = messageConverter.fromMessage(message);

        try {
            switch (typeId) {
                case TYPE_ID_BID_REQUEST_EVENT -> {
                    BidRequestEvent e = (BidRequestEvent) payload;
                    log.info("[입찰 큐 이벤트 처리] auctionSeq={}\tbidderId={}\tbidPrice={}", e.getAuctionSeq(), e.getUserId(), e.getBidPrice());
                    bidEventHandler.handleBidFromQueue(e.getAuctionSeq(), e.getUserId(), e.getBidPrice());
                }
                case TYPE_ID_BUY_NOW_REQUEST_EVENT -> {
                    BuyNowRequestEvent e = (BuyNowRequestEvent) payload;
                    log.info("[즉시 구매 큐 이벤트 처리] auctionSeq={}\tbidderId={}\tbidPrice={}", e.getAuctionSeq(), e.getUserId(), e.getBidPrice());
                    bidEventHandler.handleBuyNowFromQueue(e.getAuctionSeq(), e.getUserId());
                }
                default -> log.warn("[RabbitBidRequestListener] 알 수 없는 이벤트 타입={}", typeId);
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception ex) {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            throw ex;
        }
    }
}
