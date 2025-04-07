package com.fifteen.auction.domain.order.service;

import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.domain.auction.repository.AuctionRepository;
import com.fifteen.auction.domain.order.dto.request.CreateOrderRequest;
import com.fifteen.auction.domain.order.dto.response.OrderInfoResponse;
import com.fifteen.auction.domain.order.dto.response.OrdersResponse;
import com.fifteen.auction.domain.order.entity.Order;
import com.fifteen.auction.domain.order.repository.OrderRepository;
import com.fifteen.auction.domain.user.entity.User;
import com.fifteen.auction.domain.user.repository.UserRepository;
import com.fifteen.auction.global.dto.PageCond;
import com.fifteen.auction.global.dto.PageInfo;
import com.fifteen.auction.global.dto.Response;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;

    // 주문 정보 불러오기
    @Transactional(readOnly = true)
    @GetMapping("\"/api/v1/orders/\"+orderId+\"/payment\"")
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

    // 주문 생성
    @Transactional
    public void createOrder(CreateOrderRequest dto) {
        Auction auction = auctionRepository.findById(dto.getAuctionId())
                .orElseThrow(() -> new ClientException(ErrorCode.AUCTION_NOT_FOUNDED));

        Order order = new Order(auction);
        orderRepository.save(order);
    }

    // 주문 목록 조회
    @Transactional(readOnly = true)
    public Response<Page<OrdersResponse>> findOrders(Long loginedId, PageCond pageCond) {
        User user = userRepository.findById(loginedId)
                .orElseThrow(() -> new ClientException(ErrorCode.USER_NOT_FOUNDED));
        Pageable pageable = PageRequest.of(pageCond.getPageNum()- 1, pageCond.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> pages = orderRepository.findByUser(user, pageable);

        Page<OrdersResponse> result = pages.map(order -> new OrdersResponse(
                order.getId().toString(),
                order.getAuction().getProduct().getName(),
                order.getAuction().getWinPrice().toString(),
                order.getStatus(),
                order.getCreatedAt().toLocalDate()
        ));

        PageInfo pageInfo = PageInfo.builder()
                .pageNum(pageCond.getPageNum())
                .pageSize(pageCond.getPageSize())
                .totalPage(result.getTotalPages())
                .totalElement(result.getTotalElements())
                .build();

        return Response.of(result, pageInfo);

    }
}
