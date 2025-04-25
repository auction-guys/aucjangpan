package com.fifteen.auction.domain.auction;

import com.fifteen.auction.domain.auction.dto.event.BidProcessEvent;
import com.fifteen.auction.domain.auction.dto.event.BuyNowProcessEvent;
import com.fifteen.auction.domain.auction.dto.request.BidRequest;
import com.fifteen.auction.domain.auction.dto.response.BidHistoryInfo;
import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.domain.auction.repository.auction.AuctionRedisRepository;
import com.fifteen.auction.domain.auction.repository.auction.AuctionRepository;
import com.fifteen.auction.domain.auction.repository.bid.BidRepository;
import com.fifteen.auction.domain.auction.service.BidService;
import com.fifteen.auction.domain.auction.util.ClockHolder;
import com.fifteen.auction.global.dto.PageCond;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static com.fifteen.auction.domain.auction.entity.AuctionStatus.DONE;
import static com.fifteen.auction.fixtures.AuctionFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class BidTest {

    @Mock AuctionRepository auctionRepository;
    @Mock AuctionRedisRepository auctionRedisRepository;
    @Mock BidRepository bidRepository;

    @Mock ClockHolder clockHolder;
    @Mock ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks BidService bidService;

    public static final String ERROR_CODE_ENUM_NAME = "errorCode";

    @Nested
    class 입찰 {
        @Test
        void 입찰_은_공개된_경매에만_요청이_가능하다() {
            // given
            BidRequest bidRequest = new BidRequest(8000L);

            given(auctionRepository.findOpenOneBySeqWithSeller(anyString()))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> bidService.bid("seq", 1L, bidRequest))
                    .isInstanceOf(ClientException.class)
                    .extracting(ERROR_CODE_ENUM_NAME)
                    .isEqualTo(ErrorCode.AUCTION_NOT_FOUND);
        }

        @Test
        void 입찰_은_자신이_생성한_경매에는_요청할_수_없다() {
            // given
            Auction auction = defaultAuction(1L, 1L, "seq");
            auction.open();

            BidRequest request = new BidRequest(8000L);

            given(auctionRepository.findOpenOneBySeqWithSeller(anyString()))
                    .willReturn(Optional.of(auction));

            // when & then
            assertThatThrownBy(() -> bidService.bid("seq", 1L, request))
                    .isInstanceOf(ClientException.class)
                    .extracting(ERROR_CODE_ENUM_NAME)
                    .isEqualTo(ErrorCode.INVALID_BID_REQUEST);
        }

        @Test
        void 입찰_은_마감시각이_지난_이후에는_요청할_수_없다() {
            // given
            BidRequest request = new BidRequest(8000L);
            Auction auction = defaultAuction(1L, 1L, "seq");
            auction.open();

            given(auctionRepository.findOpenOneBySeqWithSeller(anyString()))
                    .willReturn(Optional.of(auction));

            given(clockHolder.now())
                    .willReturn(auction.getExpiresAt().plusHours(1));

            // when & then
            assertThatThrownBy(() -> bidService.bid("seq", 2L, request))
                    .isInstanceOf(ClientException.class)
                    .extracting(ERROR_CODE_ENUM_NAME)
                    .isEqualTo(ErrorCode.INVALID_BID_REQUEST);
        }

        @Test
        void 입찰_은_현재_가격보다_높은_가격에서만_이루질_수_있다() {
            // given
            BidRequest request = new BidRequest(8000L);
            Auction auction = defaultAuction(1L, 1L, "seq");
            auction.open();

            given(auctionRepository.findOpenOneBySeqWithSeller(anyString()))
                    .willReturn(Optional.of(auction));
            given(clockHolder.now())
                    .willReturn(auction.getExpiresAt().minusHours(1));
            given(auctionRedisRepository.isBidUnderPrice(anyString(), anyLong(), anyInt()))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> bidService.bid("seq", 2L, request))
                    .isInstanceOf(ClientException.class)
                    .extracting(ERROR_CODE_ENUM_NAME)
                    .isEqualTo(ErrorCode.LOW_BID_PRICE);
        }

        @Test
        void 입찰_이_완료_되면_입찰_처리_이벤트가_발행된다() {
            // given
            BidRequest request = new BidRequest(8000L);
            Auction auction = defaultAuction(1L, 1L, "seq");
            auction.open();

            given(auctionRepository.findOpenOneBySeqWithSeller(anyString()))
                    .willReturn(Optional.of(auction));
            given(clockHolder.now())
                    .willReturn(auction.getExpiresAt().minusHours(1));
            given(auctionRedisRepository.isBidUnderPrice(anyString(), anyLong(), anyInt()))
                    .willReturn(false);

            // when
            bidService.bid("seq", 2L, request);

            // then
            ArgumentCaptor<BidProcessEvent> captor = ArgumentCaptor.forClass(BidProcessEvent.class);
            verify(applicationEventPublisher, times(1)).publishEvent(captor.capture());

            assertSoftly(softly -> {
                BidProcessEvent event = captor.getValue();
                softly.assertThat(event.getBidderId()).isEqualTo(2L);
                softly.assertThat(event.getBidPrice()).isEqualTo(8000L);
                softly.assertThat(event.getAuctionSeq()).isEqualTo("seq");
                softly.assertThat(event.getBidAt()).isEqualTo(auction.getExpiresAt().minusHours(1));
            });

        }
    }

    @Nested
    class 즉시_구매 {

        @Test
        void 즉시_구매_는_공개된_경매에만_요청이_가능하다() {
            // given
            given(auctionRepository.findOpenOneBySeqWithSeller(anyString()))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> bidService.buyNow("seq", 2L))
                    .isInstanceOf(ClientException.class)
                    .extracting(ERROR_CODE_ENUM_NAME)
                    .isEqualTo(ErrorCode.AUCTION_NOT_FOUND);
        }

        @Test
        void 즉시_구매_는_즉시_구매_가_활성화_된_경매에만_요청이_가능하다() {
            // given
            Auction auction = withIsBuyNow(
                    1L, 1L, "seq", false, null);
            auction.open();

            given(auctionRepository.findOpenOneBySeqWithSeller(anyString()))
                    .willReturn(Optional.of(auction));

            // when & then
            assertThatThrownBy(() -> bidService.buyNow("seq", 2L))
                    .isInstanceOf(ClientException.class)
                    .extracting(ERROR_CODE_ENUM_NAME)
                    .isEqualTo(ErrorCode.CANNOT_BUY_NOW);
        }

        @Test
        void 즉시_구매_는_자신이_생성한_경매에는_요청할_수_없다() {
            // given
            Auction auction = withIsBuyNow(
                    1L, 1L, "seq", true, 8000L);
            auction.open();

            given(auctionRepository.findOpenOneBySeqWithSeller(anyString()))
                    .willReturn(Optional.of(auction));

            // when & then
            assertThatThrownBy(() -> bidService.buyNow("seq", 1L))
                    .isInstanceOf(ClientException.class)
                    .extracting(ERROR_CODE_ENUM_NAME)
                    .isEqualTo(ErrorCode.INVALID_BUY_NOW_REQUEST);
        }

        @Test
        void 즉시_구매_는_마감시각이_지난_이후에는_요청할_수_없다() {
            // given
            Auction auction = withIsBuyNow(
                    1L, 1L, "seq", true, 8000L);
            auction.open();

            given(auctionRepository.findOpenOneBySeqWithSeller(anyString()))
                    .willReturn(Optional.of(auction));
            given(clockHolder.now())
                    .willReturn(auction.getExpiresAt().plusHours(1));

            // when & then
            assertThatThrownBy(() -> bidService.buyNow("seq", 2L))
                    .isInstanceOf(ClientException.class)
                    .extracting(ERROR_CODE_ENUM_NAME)
                    .isEqualTo(ErrorCode.INVALID_BUY_NOW_REQUEST);
        }

        @Test
        void 즉시_구매_마감처리는_공개_상태인_경매만_가능하다() {
            // given
            Auction auction = withIsBuyNow(
                    1L, 1L, "seq", true, 8000L);

            given(auctionRepository.findOpenOneBySeqWithSeller(anyString()))
                    .willReturn(Optional.of(auction));
            given(clockHolder.now())
                    .willReturn(auction.getExpiresAt().minusHours(1));

            // when & then
            assertThatThrownBy(() -> bidService.buyNow("seq", 2L))
                    .isInstanceOf(ClientException.class)
                    .extracting(ERROR_CODE_ENUM_NAME)
                    .isEqualTo(ErrorCode.FINALIZE_ALREADY_DONE);
        }

        @Test
        void 즉시_구매_마감처리는_종료된_적이_없는_경매만_가능하다() {
            // given
            Auction auction = withIsBuyNow(
                    1L, 1L, "seq", true, 8000L);
            auction.open();
            auction.finalize(2L, 8000L, auction.getExpiresAt());

            given(auctionRepository.findOpenOneBySeqWithSeller(anyString()))
                    .willReturn(Optional.of(auction));
            given(clockHolder.now())
                    .willReturn(auction.getExpiresAt().minusHours(1));

            // when & then
            assertThatThrownBy(() -> bidService.buyNow("seq", 2L))
                    .isInstanceOf(ClientException.class)
                    .extracting(ERROR_CODE_ENUM_NAME)
                    .isEqualTo(ErrorCode.FINALIZE_ALREADY_DONE);
        }

        @Test
        void 즉시_구매_처리가_되면_낙찰_유저의_id와_낙찰가가_기록된다() {
            // given
            Auction auction = withIsBuyNow(
                    1L, 1L, "seq", true, 8000L);
            auction.open();

            given(auctionRepository.findOpenOneBySeqWithSeller(anyString()))
                    .willReturn(Optional.of(auction));
            given(clockHolder.now())
                    .willReturn(auction.getExpiresAt().minusHours(1));

            // when
            bidService.buyNow("seq", 2L);

            // then
            assertSoftly(softly -> {
                softly.assertThat(auction.getWinnerId()).isEqualTo(2L);
                softly.assertThat(auction.getWinPrice()).isEqualTo(8000L);
            });
        }

        @Test
        void 즉시_구매_처리가_되면_종료_시각을_기록하고_완료_상태로_변경한다() {
            // given
            Auction auction = withIsBuyNow(
                    1L, 1L, "seq", true, 8000L);
            auction.open();

            given(auctionRepository.findOpenOneBySeqWithSeller(anyString()))
                    .willReturn(Optional.of(auction));
            given(clockHolder.now())
                    .willReturn(auction.getExpiresAt().minusHours(1));

            // when
            bidService.buyNow("seq", 2L);

            // then
            assertSoftly(softly -> {
                softly.assertThat(auction.getDoneAt()).isEqualTo(auction.getExpiresAt().minusHours(1));
                softly.assertThat(auction.getStatus()).isEqualTo(DONE);
            });
        }

        @Test
        void 즉시_구매_처리가_되면_즉시_구매_이벤트를_발행한다() {
            // given
            Auction auction = withIsBuyNow(
                    1L, 1L, "seq", true, 8000L);
            auction.open();

            given(auctionRepository.findOpenOneBySeqWithSeller(anyString()))
                    .willReturn(Optional.of(auction));
            given(clockHolder.now())
                    .willReturn(auction.getExpiresAt().minusHours(1));

            // when
            bidService.buyNow("seq", 2L);

            // then
            ArgumentCaptor<BuyNowProcessEvent> captor = ArgumentCaptor.forClass(BuyNowProcessEvent.class);
            verify(applicationEventPublisher, times(1)).publishEvent(captor.capture());
            assertSoftly(softly -> {
                BuyNowProcessEvent event = captor.getValue();
                softly.assertThat(event.getAuctionSeq()).isEqualTo("seq");
                softly.assertThat(event.getWinnerId()).isEqualTo(2L);
                softly.assertThat(event.getBuyNowPrice()).isEqualTo(8000L);
                softly.assertThat(event.getBuyAt()).isEqualTo(auction.getExpiresAt().minusHours(1));
            });
        }
    }

    @Nested
    class 경매_기록_조회 {
        @Test
        void 경매_기록_조회가_경매_진행_중에_이루어지면_입찰_가격이_별표_처리된다() {
            // given
            Auction auction = withIsBuyNow(
                    1L, 1L, "seq", true, 8000L);
            auction.open();

            List<BidHistoryInfo> resultContent = List.of(
                    BidHistoryInfo.forProgress(defaultBid(auction, 2L, 8000L)),
                    BidHistoryInfo.forProgress(defaultBid(auction, 3L, 10000L))
            );

            given(bidRepository.findAllInProgressByAuctionSeq(any(Pageable.class), anyString()))
                    .willReturn(new PageImpl<>(resultContent));

            // when
            List<BidHistoryInfo> results = bidService
                    .bidHistoriesInProgress("seq", new PageCond(1, 10))
                    .getContent();

            // then
            assertThat(results).allSatisfy(r -> {
                assertThat(r.getBidPrice()).isEqualTo("***");
            });
        }
    }
}
