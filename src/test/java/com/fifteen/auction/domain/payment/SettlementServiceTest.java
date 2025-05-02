package com.fifteen.auction.domain.payment;

import com.fifteen.auction.domain.order.repository.OrderRepository;
import com.fifteen.auction.domain.settlement.dto.response.SettlementResponse;
import com.fifteen.auction.domain.settlement.entity.Settlement;
import com.fifteen.auction.domain.settlement.enums.SettlementStatus;
import com.fifteen.auction.domain.settlement.repository.SettlementRepository;
import com.fifteen.auction.domain.settlement.service.ChargeService;
import com.fifteen.auction.domain.settlement.service.CsvUploadService;
import com.fifteen.auction.domain.settlement.service.SettlementService;
import com.fifteen.auction.domain.user.entity.User;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import com.fifteen.auction.global.dto.exception.ServerException;
import com.fifteen.auction.infra.gMail.GmailSender;
import com.fifteen.auction.domain.order.entity.Order;
import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.domain.product.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

public class SettlementServiceTest {

    @InjectMocks
    SettlementService settlementService;

    @Mock
    SettlementRepository settlementRepository;

    @Mock
    OrderRepository orderRepository;

    @Mock
    CsvUploadService csvUploadService;

    @Mock
    GmailSender gmailSender;

    @Mock
    ChargeService chargeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        given(chargeService.getImmediatelyCharge()).willReturn(BigDecimal.valueOf(50.0));
    }

    @Nested
    @DisplayName("익일 정산 처리")
    class ImmediateSettlementTests {

        @Test
        @DisplayName("정상적인 익일 정산 처리")
        void settleImmediately_success() {
            // given
            Long settlementId = 1L;
            Long currentUserId = 1L;

            Order order = mock(Order.class);
            Auction auction = mock(Auction.class);
            Product product = mock(Product.class);
            User seller = mock(User.class);

            Long winPrice = 1000L;
            given(order.getAuction()).willReturn(auction);  // Order에서 Auction을 반환하도록 설정
            given(auction.getWinPrice()).willReturn(winPrice);  // Auction에서 경매 가격 반환
            given(auction.getProduct()).willReturn(product);  // Auction에서 Product 반환
            given(product.getSeller()).willReturn(seller);   // Product에서 Seller 반환
            given(seller.getId()).willReturn(1L);             // Seller의 ID 설정
            Settlement settlement = new Settlement(order, BigDecimal.valueOf(0.1)); // 10% 비율 적용
            settlement.settleNow(currentUserId, BigDecimal.valueOf(0.1)); // 상태를 IN_PROGRESS로 변경
            given(settlementRepository.findById(settlementId)).willReturn(Optional.of(settlement));

            // when
            assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.IN_PROGRESS); // 상태가 IN_PROGRESS여야 함
            settlement.settled();
            SettlementResponse response = settlementService.settleImmediately(settlementId, currentUserId);

            // then
            then(settlementRepository).should().findById(settlementId);
            assertThat(response).isNotNull();
            assertThat(response.getSettlementDate()).isNotNull(); // settledAt이 null이 아니어야 함
        }

        @Test
        @DisplayName("정산이 존재하지 않으면 예외 발생")
        void settleImmediately_settlementNotFound() {
            Long settlementId = 99L;
            Long currentUserId = 1L;

            // given
            given(settlementRepository.findById(settlementId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> settlementService.settleImmediately(settlementId, currentUserId))
                    .isInstanceOf(ClientException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.SETTLEMENT_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("매달 정산 처리")
    class MonthlySettlementTests {

        @Test
        @DisplayName("정상적인 매달 정산 처리")
        void settleMonthly_success() {
            // given
            Order order = mock(Order.class);
            Auction auction = mock(Auction.class);
            Product product = mock(Product.class);
            User seller = mock(User.class);

            Long winPrice = 1000L;
            given(order.getAuction()).willReturn(auction);
            given(auction.getWinPrice()).willReturn(winPrice);
            given(auction.getProduct()).willReturn(product);
            given(product.getSeller()).willReturn(seller);
            given(seller.getId()).willReturn(1L);

            Settlement settlement = new Settlement(order, BigDecimal.valueOf(0.1));
            given(settlementRepository.findAllByStatus(SettlementStatus.PENDING)).willReturn(List.of(settlement));
            given(csvUploadService.writeToCsv(anyList())).willReturn("mocked-url");

            // when
            String resultUrl = settlementService.settleMonthly();

            // then
            then(settlementRepository).should().findAllByStatus(SettlementStatus.PENDING);
            then(csvUploadService).should().writeToCsv(anyList());
            then(gmailSender).should().sendSettlement("mocked-url");
            assertThat(resultUrl).isEqualTo("mocked-url");
        }

        @Test
        @DisplayName("정산 데이터가 없으면 예외 발생")
        void settleMonthly_noData() {
            // given
            given(settlementRepository.findAllByStatus(SettlementStatus.PENDING)).willReturn(List.of());

            // when & then
            assertThatThrownBy(() -> settlementService.settleMonthly())
                    .isInstanceOf(ServerException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.SETTLEMENT_NOT_FOUND);
        }
    }
}
