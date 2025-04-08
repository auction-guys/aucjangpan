package com.fifteen.auction.domain.order.service;

import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.domain.auction.repository.AuctionRepository;
import com.fifteen.auction.domain.order.dto.request.CreateOrderRequest;
import com.fifteen.auction.domain.order.dto.response.OrderInfoResponse;
import com.fifteen.auction.domain.order.dto.response.OrderResponse;
import com.fifteen.auction.domain.order.dto.response.OrdersResponse;
import com.fifteen.auction.domain.order.entity.Order;
import com.fifteen.auction.domain.order.enums.OrderStatus;
import com.fifteen.auction.domain.order.repository.OrderRepository;
import com.fifteen.auction.domain.payment.entity.Payment;
import com.fifteen.auction.domain.payment.repository.PaymentRepository;
import com.fifteen.auction.domain.settlement.entity.Settlement;
import com.fifteen.auction.domain.settlement.repository.SettlementRepository;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final SettlementRepository settlementRepository;

    // 주문 정보 불러오기
    @Transactional(readOnly = true)
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
    public void createOrder(Long loginedId, CreateOrderRequest dto) {
        Auction auction = auctionRepository.findById(dto.getAuctionId())
                .orElseThrow(() -> new ClientException(ErrorCode.AUCTION_NOT_FOUNDED));
        User user = userRepository.findById(loginedId)
                .orElseThrow(() -> new ClientException(ErrorCode.USER_NOT_FOUNDED));

        if(!auction.getWinnerId().equals(loginedId)){
            throw new ClientException(ErrorCode.AUCTION_ACCESS_DENIED);
        }

        Order order = new Order(auction, user);
        orderRepository.save(order);
    }

    // 주문 목록 조회
    @Transactional(readOnly = true)
    public Response<Page<OrdersResponse>> findOrders(Long loginedId, PageCond pageCond) {
        User user = userRepository.findById(loginedId)
                .orElseThrow(() -> new ClientException(ErrorCode.USER_NOT_FOUNDED));
        Pageable pageable = PageRequest.of(pageCond.getPageNum()- 1, pageCond.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> pages = orderRepository.findByUser(user, pageable);

        Page<OrdersResponse> result = pages.map(order -> OrdersResponse.builder()
                                .orderId(order.toString())
                                .productName(order.getAuction().getProduct().getName())
                                .amount(order.getAuction().getWinPrice().toString())
                                .status(order.getStatus())
                                .orderedDate(order.getCreatedAt().toLocalDate())
                                .build()
        );

        PageInfo pageInfo = PageInfo.builder()
                .pageNum(pageCond.getPageNum())
                .pageSize(pageCond.getPageSize())
                .totalPage(result.getTotalPages())
                .totalElement(result.getTotalElements())
                .build();

        return Response.of(result, pageInfo);

    }

    // 주문 내역 상세 조회
    @Transactional(readOnly = true)
    public OrderResponse findOrder(Long loginedId, String orderId) {
        User user = userRepository.findById(loginedId)
                .orElseThrow(() -> new ClientException(ErrorCode.USER_NOT_FOUNDED));
        Order order = orderRepository.findById(Long.parseLong(orderId))
                .orElseThrow(() -> new ClientException(ErrorCode.ORDER_NOT_FOUNDED));
        Payment payment = paymentRepository.findByOrderId(Long.parseLong(orderId))
                .orElseThrow(() -> new ClientException(ErrorCode.PAYMENT_NOT_FOUNDED));

        if(!user.getId().equals(order.getAuction().getWinnerId())){
            throw new ClientException(ErrorCode.ORDER_ACCESS_DENIDED);
        }

        return OrderResponse.builder()
                .name(user.getName())
                .orderId(orderId)
                .address(user.getAddress())
                .paymentType(payment.getPaymentMethod())
                .productName(order.getAuction().getProduct().getName())
                .amount(order.getAuction().getWinPrice().toString())
                .status(order.getStatus())
                .orderedDate(order.getCreatedAt().toLocalDate())
                .build();
    }

    @Transactional
    public void cancelOrder(Long loginedId, String orderId) {
        User user = userRepository.findById(loginedId)
                .orElseThrow(() -> new ClientException(ErrorCode.USER_NOT_FOUNDED));
        Order order = orderRepository.findById(Long.parseLong(orderId))
                .orElseThrow(() -> new ClientException(ErrorCode.ORDER_NOT_FOUNDED));

        if(!user.getId().equals(order.getAuction().getWinnerId())){
            throw new ClientException(ErrorCode.ORDER_ACCESS_DENIDED);
        }

        // 여기에 취소시 회원데이터에 경고 카운트가 올라가거나 하는거 있음 좋을듯
        order.cancel();
    }

    // 구매 확정
    @Transactional
    public void confirmOrder(Long loginedId, String orderId) {
        User user = userRepository.findById(loginedId)
                .orElseThrow(() -> new ClientException(ErrorCode.USER_NOT_FOUNDED));
        Order order = orderRepository.findById(Long.parseLong(orderId))
                .orElseThrow(() -> new ClientException(ErrorCode.ORDER_NOT_FOUNDED));

        if(!user.getId().equals(order.getAuction().getWinnerId())){
            throw new ClientException(ErrorCode.ORDER_ACCESS_DENIDED);
        }
        if(!order.getStatus().equals(OrderStatus.PAID)){
            throw new ClientException(ErrorCode.ORDER_ALREADY_PROCESSED);
        }

        order.confirm();

        // 정산 데이터 생성
        Settlement settlement = new Settlement(order);
        settlementRepository.save(settlement);
    }
}
