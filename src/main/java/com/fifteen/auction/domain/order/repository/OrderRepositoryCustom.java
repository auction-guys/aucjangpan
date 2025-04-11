package com.fifteen.auction.domain.order.repository;

import com.fifteen.auction.domain.order.dto.response.OrderResponse;
import com.fifteen.auction.domain.order.dto.response.OrdersResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface OrderRepositoryCustom {

    Page<OrdersResponse> findAllByUserId(Long currentUserId, Pageable pageable);

    Optional<OrderResponse> findByOrderIdAndUserId(Long orderId, Long currentUserId);
}
