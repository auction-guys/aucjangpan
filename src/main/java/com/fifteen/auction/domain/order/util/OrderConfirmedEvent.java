package com.fifteen.auction.domain.order.util;

import com.fifteen.auction.domain.order.entity.Order;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OrderConfirmedEvent {

    private final Order order;
}
