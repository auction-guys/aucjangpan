package com.fifteen.auction.domain.order.service;

import com.fifteen.auction.domain.order.dto.response.OrderInfoResponse;
import com.fifteen.auction.domain.order.entity.Order;
import com.fifteen.auction.domain.order.repository.OrderRepository;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderInfoResponse getOrderInfo(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ClientException(ErrorCode.ORDER_NOT_FOUNDED));

        return new OrderInfoResponse(
                orderId.toString(),
                order.getAuction().getProduct().getName(),
                order.getAuction().getProduct().getSeller().getEmail(),
                order.getAuction().getProduct().getSeller().getName(),
                order.getAuction().getWinPrice().toString()
        );
    }
}
