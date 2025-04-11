package com.fifteen.auction.domain.order.service;

import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.domain.auction.repository.auction.AuctionRepository;
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
import org.springframework.transaction.annotation.Transactional;

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

        // TODO 나중에 여기에도 회원인증 필요할듯 나중에 다시 생각
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ClientException(ErrorCode.ORDER_NOT_FOUND));

        return OrderInfoResponse.from(order);
    }

    // 주문 생성
    // TODO api 고민 CreateOrderRequest에 auctionId밖에 없는데 PathVariable로 받거나 RequsetParam으로 받아도 되지 않은지
    @Transactional
    public void createOrder(Long currentUserId, CreateOrderRequest dto) {
        Auction auction = auctionRepository.findById(dto.getAuctionId())
                .orElseThrow(() -> new ClientException(ErrorCode.AUCTION_NOT_FOUND));

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
        //TODO 아래 주석처리한 부분 굳이 필요한가? 어차피 시큐리티로 1차로 회원만 들어오고 밑에 조회 조건을 로그인 아이디만 하면 본인 주문만 볼 수 있으니까 필요 없을듯?
//        User user = userRepository.findById(currentUserId)
//                .orElseThrow(() -> new ClientException(ErrorCode.USER_NOT_FOUND));
        Pageable pageable = PageRequest.of(pageCond.getPageNum() - 1, pageCond.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OrdersResponse> pages = orderRepository.findAllByUserId(currentUserId, pageable);

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
        OrderResponse orderResponse = orderRepository.findByOrderIdAndUserId(Long.parseLong(orderId), currentUserId)
                .orElseThrow(() -> new ClientException(ErrorCode.ORDER_NOT_FOUND));

        return orderResponse;
    }

    @Transactional
    public void cancelOrder(Long currentUserId, String orderId) {
        Order order = orderRepository.findById(Long.parseLong(orderId))
                .orElseThrow(() -> new ClientException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(currentUserId)) {
            throw new ClientException(ErrorCode.ORDER_ACCESS_DENIED);
        }

        // TODO 여기에 취소시 회원데이터에 경고 카운트가 올라가거나 하는거 있음 좋을듯
        order.cancel();
    }

    // 구매 확정
    @Transactional
    public void confirmOrder(Long currentUserId, String orderId) {
        Order order = orderRepository.findById(Long.parseLong(orderId))
                .orElseThrow(() -> new ClientException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(currentUserId)) {
            throw new ClientException(ErrorCode.ORDER_ACCESS_DENIED);
        }
        if (!order.getStatus().equals(OrderStatus.PAID)) {
            throw new ClientException(ErrorCode.ORDER_ALREADY_PROCESSED);
        }

        order.confirm();

        // 정산 데이터 생성
        Settlement settlement = new Settlement(order);
        settlementRepository.save(settlement);
    }
}
