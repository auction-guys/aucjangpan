package com.fifteen.auction.domain.auction.infrastructure;

import com.fifteen.auction.domain.auction.dto.event.BidRequestEvent;
import com.fifteen.auction.domain.auction.service.BidService;
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

    private final BidService bidService;

    @RabbitListener(queues = "#{rabbitConfig.queueNames}")
    public void bidRequestListener(BidRequestEvent e, Channel channel, Message raw) throws IOException {
        try {
            log.info("[입찰 큐 이벤트 처리] auctionSeq={}\tbidderId={}\tbidPrice={}", e.getAuctionSeq(), e.getUserId(), e.getBidPrice());
            bidService.handleBidFromQueue(e.getAuctionSeq(), e.getUserId(), e.getBidPrice());
            channel.basicAck(raw.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception ex) {
            channel.basicNack(raw.getMessageProperties().getDeliveryTag(), false, false);
            throw ex;
        }
    }

}
