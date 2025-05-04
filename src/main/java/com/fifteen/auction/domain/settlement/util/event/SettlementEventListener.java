package com.fifteen.auction.domain.settlement.util.event;

import com.fifteen.auction.domain.order.util.OrderConfirmedEvent;
import com.fifteen.auction.domain.settlement.entity.Settlement;
import com.fifteen.auction.domain.settlement.repository.SettlementRepository;
import com.fifteen.auction.domain.settlement.service.ChargeService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SettlementEventListener {

    private final SettlementRepository settlementRepository;
    private final ChargeService chargeService;

    @Async
    @EventListener
    @Transactional
    public void handlerOrderConfirmed(OrderConfirmedEvent event){

        Settlement settlement = new Settlement(event.getOrder(), chargeService.getAutoCharge());
        settlementRepository.save(settlement);
    }
}
