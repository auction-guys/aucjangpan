package com.fifteen.auction.domain.order.service;

import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.domain.auction.repository.auction.AuctionRepository;
import com.fifteen.auction.domain.order.dto.response.OrderInfoResponse;
import com.fifteen.auction.domain.order.dto.response.OrderResponse;
import com.fifteen.auction.domain.order.dto.response.OrdersResponse;
import com.fifteen.auction.domain.order.entity.Order;
import com.fifteen.auction.domain.order.repository.OrderRepository;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final SettlementRepository settlementRepository;

    // TODO: 이후 정산 이벤트 처리시 옮기거나 테이블 분리해서 구현 예정
    @Value("${settlement.charge.scheduler}")
    private double scheduler;

    // 주문 정보 불러오기
    @Transactional(readOnly = true)
    public OrderInfoResponse getOrderInfo(String orderId) {

        // TODO 나중에 여기에도 회원인증 필요할듯 나중에 다시 생각
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ClientException(ErrorCode.ORDER_NOT_FOUND));

        return OrderInfoResponse.from(order);
    }

    // 주문 생성
    @Transactional
    public void createOrder(Long currentUserId, Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ClientException(ErrorCode.AUCTION_NOT_FOUND));

        // TODO 여기도 책임분산 되려나? 나중에 질문하기
        // 유저 검증
        if (!auction.getWinnerId().equals(currentUserId)) {
            throw new ClientException(ErrorCode.AUCTION_ACCESS_DENIED);
        }

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ClientException(ErrorCode.USER_NOT_FOUND));

        Order order = new Order(auction, user);
        orderRepository.save(order);
    }

    // 주문 목록 조회
    @Transactional(readOnly = true)
    public Response<Page<OrdersResponse>> findOrders(Long currentUserId, PageCond pageCond) {

        Pageable pageable = PageRequest.of(pageCond.getPageNum() - 1, pageCond.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OrdersResponse> pages = orderRepository.findAllByUserId(currentUserId, pageable);

        // TODO 이거는 같이 쓰는거라 내가 해도 될지 일단 후순위
        PageInfo pageInfo = PageInfo.builder()
                .pageNum(pageCond.getPageNum())
                .pageSize(pageCond.getPageSize())
                .totalPage(pages.getTotalPages())
                .totalElement(pages.getTotalElements())
                .build();

        return Response.of(pages, pageInfo);

    }

    // 주문 내역 상세 조회
    @Transactional(readOnly = true)
    public OrderResponse findOrder(Long currentUserId, String orderId) {

        // TODO: 이거 에러 메시지 뭘로할지 아니면 그냥 엔티티로 받아서 검증을 한번 할지

        return orderRepository.findByOrderIdAndUserId(orderId, currentUserId)
                .orElseThrow(() -> new ClientException(ErrorCode.ORDER_NOT_FOUND));
    }

    // 주문 취소
    @Transactional
    public void cancelOrder(Long currentUserId, String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ClientException(ErrorCode.ORDER_NOT_FOUND));

        // TODO 여기에 취소시 회원데이터에 경고 카운트가 올라가거나 하는거 있음 좋을듯
        order.cancel(currentUserId);
    }

    // 구매 확정
    @Transactional
    public void confirmOrder(Long currentUserId, String orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ClientException(ErrorCode.ORDER_NOT_FOUND));

        order.confirm(currentUserId);

        // TODO 이벤트로 처리 - 고도화
        Settlement settlement = new Settlement(order, scheduler);
        settlementRepository.save(settlement);
    }
}
