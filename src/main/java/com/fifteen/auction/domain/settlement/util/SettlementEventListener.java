package com.fifteen.auction.domain.settlement.util;

import com.fifteen.auction.domain.order.util.OrderConfirmedEvent;
import com.fifteen.auction.domain.settlement.entity.Settlement;
import com.fifteen.auction.domain.settlement.repository.SettlementRepository;
import com.fifteen.auction.domain.settlement.service.ChargeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SettlementEventListener {

    private final SettlementRepository settlementRepository;
    private final ChargeService chargeService;

    @TransactionalEventListener
    public void handlerOrderConfirmed(OrderConfirmedEvent event){

        Settlement settlement = new Settlement(event.getOrder(), chargeService.getAutoCharge());
        settlementRepository.save(settlement);

    }
}
