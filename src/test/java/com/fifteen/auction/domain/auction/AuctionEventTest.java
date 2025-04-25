package com.fifteen.auction.domain.auction;

import com.fifteen.auction.domain.auction.dto.event.BidProcessEvent;
import com.fifteen.auction.domain.auction.dto.event.BuyNowEvent;
import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.domain.auction.repository.auction.AuctionRedisRepository;
import com.fifteen.auction.domain.auction.repository.auction.AuctionRepository;
import com.fifteen.auction.domain.auction.service.AuctionScheduledService;
import com.fifteen.auction.domain.auction.service.AuctionService;
import com.fifteen.auction.domain.auction.service.BidEventService;
import com.fifteen.auction.domain.inbox.dto.CreateMessageRequest;
import com.fifteen.auction.domain.inbox.service.InboxService;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.fifteen.auction.domain.auction.entity.AuctionStatus.DONE;
import static com.fifteen.auction.domain.auction.entity.AuctionStatus.MISCARRY;
import static com.fifteen.auction.fixtures.AuctionFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuctionEventTest {

    public static final String ERROR_CODE_ENUM_NAME = "errorCode";

    @Nested
    class 입찰_이벤트 {
        @Mock AuctionRepository auctionRepository;
        @Mock AuctionRedisRepository auctionRedisRepository;

        @InjectMocks BidEventService bidEventService;

        @Test
        void 입찰_이벤트_처리_중_저동_연장이_설정된_경매만_연장_처리가_된다() {
            // given
            LocalDateTime originalExpiresAt = LocalDateTime.now().plusHours(2L);
            Auction auction = withIsAutoExtensible(
                    1L, 1L, "seq", false, originalExpiresAt);
            BidProcessEvent event = new BidProcessEvent(
                    "seq", 2L, 2000L, auction.getExpiresAt().minusSeconds(59));

            given(auctionRepository.findOpenOneByAuctionSeq(anyString()))
                    .willReturn(Optional.of(auction));

            // when
            bidEventService.handleBidProcess(event);

            // then
            assertThat(auction.getExpiresAt()).isEqualTo(originalExpiresAt);
        }

        @Test
        void 입찰_이벤트_처리_중_입찰_시각이_자동_연장_경매의_마감까지_1분_이내_남았다면_3분_연장된다() {
            // given
            LocalDateTime originalExpiresAt = LocalDateTime.now().plusHours(2L);
            Auction auction = withIsAutoExtensible(
                    1L, 1L, "seq", true, originalExpiresAt);
            BidProcessEvent event = new BidProcessEvent(
                    "seq", 2L, 2000L, auction.getExpiresAt().minusSeconds(59));

            given(auctionRepository.findOpenOneByAuctionSeq(anyString()))
                    .willReturn(Optional.of(auction));

            // when
            bidEventService.handleBidProcess(event);

            // then
            assertThat(auction.getExpiresAt()).isEqualTo(originalExpiresAt.plusMinutes(3));
        }

        @Test
        void 입찰_이벤트_가_처리될_때_자동_연장_경매는_한_번만_연장이_가능하다() {
            // given
            LocalDateTime originalExpiresAt = LocalDateTime.now().plusHours(2L);
            Auction auction = withIsAutoExtensible(
                    1L, 1L, "seq", true, originalExpiresAt);

            BidProcessEvent event1 = new BidProcessEvent(
                    "seq", 2L, 2000L, auction.getExpiresAt().minusSeconds(59));
            BidProcessEvent event2 = new BidProcessEvent(
                    "seq", 3L, 4000L, auction.getExpiresAt().minusSeconds(30));

            given(auctionRepository.findOpenOneByAuctionSeq(anyString()))
                    .willReturn(Optional.of(auction));

            // when
            bidEventService.handleBidProcess(event1);
            bidEventService.handleBidProcess(event2);

            // then
            assertThat(auction.getExpiresAt()).isEqualTo(originalExpiresAt.plusMinutes(3));
        }

        @Test
        void 입찰_이벤트_가_처리되면_경매_현재가가_최신화_된다() {
            // given
            LocalDateTime originalExpiresAt = LocalDateTime.now().plusHours(2L);
            Auction auction = withIsAutoExtensible(
                    1L, 1L, "seq", true, originalExpiresAt);

            BidProcessEvent event = new BidProcessEvent(
                    "seq", 2L, 2000L, auction.getExpiresAt().minusSeconds(59));

            given(auctionRepository.findOpenOneByAuctionSeq(anyString()))
                    .willReturn(Optional.of(auction));

            // when
            bidEventService.handleBidProcess(event);

            // then
            verify(auctionRedisRepository, times(1))
                    .addNewHighPrice(eq("seq"), eq(2L), eq(2000L));
        }

    }

    @Nested
    class 종료된_경매 {
        AuctionRepository auctionRepository;

        AuctionRedisRepository auctionRedisRepository;
        InboxService inboxService;

        AuctionScheduledService auctionScheduledService;

        public 종료된_경매() {
            auctionRepository = mock(AuctionRepository.class);
            auctionRedisRepository = mock(AuctionRedisRepository.class);

            inboxService = mock(InboxService.class);
            auctionScheduledService = new AuctionScheduledService(
                    mock(TaskScheduler.class),
                    auctionRedisRepository,
                    spy(new AuctionService(
                            auctionRepository,
                            null, null, null,
                            auctionRedisRepository,
                            null, null, null
                    )),
                    inboxService
            );
        }

        @Test
        void 종료된_경매_가_유찰_처리_될_때_그_경매는_공개_상태여야만_한다() {
            // given
            Auction auction = withStartPrice(1L, 1L, "seq", 8000L);
            ReflectionTestUtils.setField(auction, "id", 1L);

            given(auctionRedisRepository.findCurrentPrice(anyString())).willReturn(8000L);
            given(auctionRepository.findById(1L)).willReturn(Optional.of(auction));

            // when & then
            assertThatThrownBy(() -> auctionScheduledService.handleExpiration(1L, "seq", 8000L))
                    .isInstanceOf(ClientException.class)
                    .extracting(ERROR_CODE_ENUM_NAME)
                    .isEqualTo(ErrorCode.AUCTION_NOT_OPEN);
        }

        @Test
        void 종료된_경매_가_유찰_처리_되면_유찰_상태가_되고_만료_시각이_종료_시각이_된다() {
            // given
            Auction auction = withStartPrice(1L, 1L, "seq", 8000L);
            ReflectionTestUtils.setField(auction, "id", 1L);
            auction.open();

            given(auctionRedisRepository.findCurrentPrice(anyString())).willReturn(8000L);
            given(auctionRepository.findById(1L)).willReturn(Optional.of(auction));

            // when
            auctionScheduledService.handleExpiration(1L, "seq", 8000L);

            // then
            assertSoftly(softly -> {
                softly.assertThat(auction.getStatus()).isEqualTo(MISCARRY);
                softly.assertThat(auction.getDoneAt()).isEqualTo(auction.getExpiresAt());
            });
        }

        @Test
        void 종료된_경매_의_최고_입찰자는_낙찰자가_되고_그_입찰가는_낙찰가가_된다() {
            // given
            Auction auction = withStartPrice(1L, 1L, "seq", 8000L);
            ReflectionTestUtils.setField(auction, "id", 1L);
            auction.open();

            given(auctionRedisRepository.findCurrentPrice(anyString())).willReturn(10000L);
            given(auctionRedisRepository.findParticipants("seq"))
                    .willReturn(List.of(2L, 3L, 4L));
            given(auctionRepository.findOpenOneByAuctionSeq(anyString()))
                    .willReturn(Optional.of(auction));

            // when
            auctionScheduledService.handleExpiration(1L, "seq", 8000L);

            // then
            assertSoftly(softly -> {
                softly.assertThat(auction.getWinnerId()).isEqualTo(2L);
                softly.assertThat(auction.getWinPrice()).isEqualTo(10000L);
                softly.assertThat(auction.getDoneAt()).isEqualTo(auction.getExpiresAt());
                softly.assertThat(auction.getStatus()).isEqualTo(DONE);
            });
        }

        @Test
        void 종료된_경매_의_낙찰자의_inbox에는_낙찰_메시지가_저장된다() {
            // given
            Auction auction = withStartPrice(1L, 1L, "seq", 8000L);
            ReflectionTestUtils.setField(auction, "id", 1L);
            auction.open();

            given(auctionRedisRepository.findCurrentPrice(anyString())).willReturn(10000L);
            given(auctionRedisRepository.findParticipants("seq"))
                    .willReturn(List.of(2L, 3L, 4L));
            given(auctionRepository.findOpenOneByAuctionSeq(anyString()))
                    .willReturn(Optional.of(auction));

            String expectedMessage = CreateMessageRequest.forWinner(2L, "seq")
                    .getMessage();

            // when
            auctionScheduledService.handleExpiration(1L, "seq", 8000L);

            // then
            ArgumentCaptor<CreateMessageRequest> captor = ArgumentCaptor.forClass(CreateMessageRequest.class);
            verify(inboxService, times(1)).addSingleMessage(captor.capture());
            assertThat(captor.getValue().getMessage()).isEqualTo(expectedMessage);
        }

        @Test
        void 종료된_경매_의_참가자의_inbox에는_결과_메시지가_저장된다() {
            // given
            Auction auction = withStartPrice(1L, 1L, "seq", 8000L);
            ReflectionTestUtils.setField(auction, "id", 1L);
            auction.open();

            given(auctionRedisRepository.findCurrentPrice(anyString())).willReturn(10000L);
            given(auctionRedisRepository.findParticipants("seq"))
                    .willReturn(List.of(2L, 3L, 4L));
            given(auctionRepository.findOpenOneByAuctionSeq(anyString()))
                    .willReturn(Optional.of(auction));

            String expectedMessage = CreateMessageRequest.forParticipants(2L, "seq")
                    .getMessage();

            // when
            auctionScheduledService.handleExpiration(1L, "seq", 8000L);

            // then
            ArgumentCaptor<List<CreateMessageRequest>> captor = ArgumentCaptor.forClass(List.class);
            verify(inboxService, times(1)).addMultipleMessages(captor.capture());
            assertThat(captor.getValue()).allSatisfy(r -> {
                r.getMessage().equals(expectedMessage);
            });
        }

        @Test
        void 즉시_구매_시_구매자의_inbox에는_낙찰_메시지가_저장된다() {
            // given
            Auction auction = withIsBuyNow(1L, 1L, "seq", true, 60000L);
            ReflectionTestUtils.setField(auction, "id", 1L);
            auction.open();
            auction.finalize(2L, 60000L, auction.getExpiresAt().minusHours(2));

            String expectedMessage = CreateMessageRequest.forWinner(2L, "seq")
                    .getMessage();

            BuyNowEvent buyNowEvent = BuyNowEvent.fromAuction(auction);

            // when
            auctionScheduledService.processBuyNowMessaging(buyNowEvent);

            // then
            ArgumentCaptor<CreateMessageRequest> captor = ArgumentCaptor.forClass(CreateMessageRequest.class);
            verify(inboxService, times(1)).addSingleMessage(captor.capture());
            assertThat(captor.getValue().getMessage()).isEqualTo(expectedMessage);
        }

        @Test
        void 즉시_구매_시_참가자의_inbox에는_결과_메시지가_저장된다() {
            // given
            Auction auction = withIsBuyNow(1L, 1L, "seq", true, 60000L);
            ReflectionTestUtils.setField(auction, "id", 1L);
            auction.open();
            auction.finalize(2L, 60000L, auction.getExpiresAt().minusHours(2));

            BuyNowEvent buyNowEvent = BuyNowEvent.fromAuction(auction);
            String expectedMessage = CreateMessageRequest.forParticipants(2L, "seq")
                    .getMessage();

            given(auctionRedisRepository.findParticipants("seq"))
                    .willReturn(List.of(3L, 4L));

            // when
            auctionScheduledService.processBuyNowMessaging(buyNowEvent);

            // then
            ArgumentCaptor<List<CreateMessageRequest>> captor = ArgumentCaptor.forClass(List.class);
            verify(inboxService, times(1)).addMultipleMessages(captor.capture());
            assertThat(captor.getValue()).allSatisfy(r -> {
                r.getMessage().equals(expectedMessage);
            });
        }
    }
}
