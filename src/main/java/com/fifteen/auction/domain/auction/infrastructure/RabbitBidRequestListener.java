package com.fifteen.auction.domain.auction.infrastructure;

import com.fifteen.auction.domain.auction.dto.event.BidRequestEvent;
import com.fifteen.auction.domain.auction.dto.event.BuyNowRequestEvent;
import com.fifteen.auction.domain.auction.service.port.in.BidEventUseCase;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j @Component
@RequiredArgsConstructor
public class RabbitBidRequestListener {

    private final BidEventUseCase bidEventUseCase;

    @RabbitListener(queues = "#{rabbitConfig.queueNames}")
    public void bidRequestListener(BidRequestEvent e, Channel channel, Message raw) throws IOException {
        try {
            log.info("[입찰 큐 이벤트 처리] auctionSeq={}\tbidderId={}\tbidPrice={}", e.getAuctionSeq(), e.getUserId(), e.getBidPrice());
            bidEventUseCase.handleBidFromQueue(e.getAuctionSeq(), e.getUserId(), e.getBidPrice());
            channel.basicAck(raw.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception ex) {
            channel.basicNack(raw.getMessageProperties().getDeliveryTag(), false, false);
            throw ex;
        }
    }

    @RabbitListener(queues = "#{rabbitConfig.queueNames}")
    public void buyNowRequestListener(BuyNowRequestEvent e, Channel channel, Message raw) throws IOException {
        try {
            log.info("[즉시 구매 큐 이벤트 처리] auctionSeq={}\tbidderId={}\tbidPrice={}", e.getAuctionSeq(), e.getUserId(), e.getBidPrice());
            bidEventUseCase.handleBuyNowFromQueue(e.getAuctionSeq(), e.getUserId());
            channel.basicAck(raw.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception ex) {
            channel.basicNack(raw.getMessageProperties().getDeliveryTag(), false, false);
            throw ex;
        }
    }
}
