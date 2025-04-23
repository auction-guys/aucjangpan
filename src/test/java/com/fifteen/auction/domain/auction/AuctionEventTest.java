package com.fifteen.auction.domain.auction;

import com.fifteen.auction.domain.auction.dto.event.BidProcessEvent;
import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.domain.auction.repository.auction.AuctionRepository;
import com.fifteen.auction.domain.auction.service.AuctionCacheService;
import com.fifteen.auction.domain.auction.service.BidWorkerService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.fifteen.auction.fixtures.AuctionFixture.withIsAutoExtensible;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AuctionEventTest {

    @Nested
    class 입찰_이벤트 {

        @Mock AuctionRepository auctionRepository;
        @Mock AuctionCacheService auctionCacheService;

        @InjectMocks BidWorkerService bidWorkerService;

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
            bidWorkerService.handleBidProcess(event);

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
            bidWorkerService.handleBidProcess(event);

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
            bidWorkerService.handleBidProcess(event1);
            bidWorkerService.handleBidProcess(event2);

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
            bidWorkerService.handleBidProcess(event);

            // then
            verify(auctionCacheService, times(1))
                    .addNewHighPrice(eq("seq"), eq(2L), eq(2000L));
        }

    }
}
