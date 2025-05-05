package com.fifteen.auction.domain.payment;

import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.domain.auction.repository.auction.AuctionRepository;
import com.fifteen.auction.domain.order.entity.Order;
import com.fifteen.auction.domain.order.enums.OrderStatus;
import com.fifteen.auction.domain.order.repository.OrderRepository;
import com.fifteen.auction.domain.order.service.OrderService;
import com.fifteen.auction.domain.order.util.OrderConfirmedEvent;
import com.fifteen.auction.domain.user.entity.User;
import com.fifteen.auction.domain.user.repository.UserRepository;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    OrderService orderService;

    @Mock
    OrderRepository orderRepository;
    @Mock
    AuctionRepository auctionRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    ApplicationEventPublisher publisher;

    @Nested @DisplayName("CreateOrder 기본/추가 시나리오")
    class CreateOrderTests {

        @Test @DisplayName("정상 주문 생성")
        void success() {
            Long userId = 1L, auctionId = 2L;
            Auction auction = mock(Auction.class);
            given(auction.getWinnerId()).willReturn(userId);
            given(auction.getAuctionSeq()).willReturn("SEQ-1");
            given(auctionRepository.findById(auctionId)).willReturn(Optional.of(auction));
            User user = mock(User.class);
            given(user.getId()).willReturn(userId);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(orderRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            orderService.createOrder(userId, auctionId);

            ArgumentCaptor<Order> order = ArgumentCaptor.forClass(Order.class);
            then(orderRepository).should().save(order.capture());
            Order savedOrder = order.getValue();
            assertThat(savedOrder.getId()).isEqualTo("SEQ-1");
            assertThat(savedOrder.getUser().getId()).isEqualTo(userId);
            assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
        }

        @Test @DisplayName("존재하지 않는 경매 예외")
        void auctionNotFound() {
            given(auctionRepository.findById(anyLong())).willReturn(Optional.empty());
            assertThatThrownBy(() -> orderService.createOrder(1L, 999L))
                    .isInstanceOf(ClientException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.AUCTION_NOT_FOUND);
        }

        @Test @DisplayName("낙찰자 아니면 예외")
        void notWinner() {
            Long userId = 1L, auctionId = 2L;
            Auction auction = mock(Auction.class);
            given(auction.getWinnerId()).willReturn(999L);
            given(auctionRepository.findById(auctionId)).willReturn(Optional.of(auction));

            assertThatThrownBy(() -> orderService.createOrder(userId, auctionId))
                    .isInstanceOf(ClientException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.AUCTION_ACCESS_DENIED);
        }

        @Test @DisplayName("존재하지 않는 유저 예외")
        void userNotFound() {
            Long userId = 1L, auctionId = 2L;
            Auction auction = mock(Auction.class);
            given(auction.getWinnerId()).willReturn(userId);
            given(auctionRepository.findById(auctionId)).willReturn(Optional.of(auction));
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.createOrder(userId, auctionId))
                    .isInstanceOf(ClientException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.USER_NOT_FOUND);
        }

        @Test @DisplayName("save() 한 번만 호출")
        void saveCalledOnce() {
            Long userId = 1L, auctionId = 2L;
            Auction auction = mock(Auction.class);
            given(auction.getWinnerId()).willReturn(userId);
            given(auctionRepository.findById(auctionId)).willReturn(Optional.of(auction));
            User user = mock(User.class);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            orderService.createOrder(userId, auctionId);

            then(orderRepository).should(times(1)).save(any(Order.class));
        }
    }

    @Nested @DisplayName("ConfirmOrder 이벤트 퍼블리시 검증")
    class ConfirmOrderEventTests {

        @Test @DisplayName("confirmOrder() 호출 시 OrderConfirmedEvent 퍼블리시")
        void confirmOrder_publishesEvent() {
            // given
            String orderId = "order-123";
            Long userId = 42L;
            Order order = mock(Order.class);
            given(orderRepository.findByIdFetchAuction(orderId)).willReturn(Optional.of(order));

            // when
            orderService.confirmOrder(userId, orderId);

            // then
            ArgumentCaptor<OrderConfirmedEvent> event = ArgumentCaptor.forClass(OrderConfirmedEvent.class);
            then(publisher).should().publishEvent(event.capture());

            OrderConfirmedEvent published = event.getValue();
            assertThat(published.getOrder()).isSameAs(order);
        }

        @Test @DisplayName("존재하지 않는 주문일 때 예외 발생")
        void confirmOrder_nonexistentOrder_throws() {
            // given
            String orderId = "nope";
            Long userId = 42L;
            given(orderRepository.findByIdFetchAuction(orderId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.confirmOrder(userId, orderId))
                    .isInstanceOf(ClientException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.ORDER_NOT_FOUND);
        }
    }
}