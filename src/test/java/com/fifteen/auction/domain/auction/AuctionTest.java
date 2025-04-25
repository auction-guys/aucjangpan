package com.fifteen.auction.domain.auction;

import com.fifteen.auction.domain.auction.dto.event.AuctionOpenEvent;
import com.fifteen.auction.domain.auction.dto.request.AuctionCreateRequest;
import com.fifteen.auction.domain.auction.dto.request.AuctionUpdateRequest;
import com.fifteen.auction.domain.auction.dto.response.AuctionLog;
import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.domain.auction.repository.auction.AuctionRedisRepository;
import com.fifteen.auction.domain.auction.repository.auction.AuctionRepository;
import com.fifteen.auction.domain.auction.repository.bid.BidRepository;
import com.fifteen.auction.domain.auction.service.AuctionService;
import com.fifteen.auction.domain.auction.util.AuctionSeqGenerator;
import com.fifteen.auction.domain.auction.util.ClockHolder;
import com.fifteen.auction.domain.product.entity.Product;
import com.fifteen.auction.domain.product.repository.ProductRepository;
import com.fifteen.auction.domain.product.service.MarketPriceService;
import com.fifteen.auction.fixtures.ProductFixture;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.fifteen.auction.domain.auction.entity.AuctionStatus.CANCELED;
import static com.fifteen.auction.domain.auction.entity.AuctionStatus.OPEN;
import static com.fifteen.auction.fixtures.AuctionFixture.defaultAuction;
import static com.fifteen.auction.fixtures.AuctionFixture.fromCreateDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AuctionTest {

    public static final String ERROR_CODE_ENUM_NAME = "errorCode";
    @Mock ProductRepository productRepository;
    @Mock AuctionRepository auctionRepository;
    @Mock BidRepository bidRepository;

    @Mock MarketPriceService marketPriceService;
    @Mock AuctionRedisRepository auctionRedisRepository;

    @Mock ApplicationEventPublisher applicationEventPublisher;
    @Mock AuctionSeqGenerator auctionSeqGenerator;
    @Mock ClockHolder clockHolder;

    @InjectMocks AuctionService auctionService;


    @Nested
    class 경매_생성 {
        @Test
        void 경매_생성_이_성공적으로_이뤄진다() {
            // given
            Product product = ProductFixture.ofUser(1L, 1L);
            AuctionCreateRequest dto = new AuctionCreateRequest(
                    1L,
                    10000L,
                    100000L,
                    1000,
                    true,
                    true,
                    LocalDateTime.now().plusHours(2)
            );

            given(productRepository.findByIdWithSeller(anyLong())).willReturn(Optional.of(product));
            given(auctionSeqGenerator.generate(any(LocalDate.class))).willReturn("auctionSeq");
            given(auctionRepository.save(any(Auction.class)))
                    .willReturn(fromCreateDto(dto, 1L, "auctionSeq"));

            // when
            String auctionSeq = auctionService.create(dto, 1L);

            // then
            assertThat(auctionSeq).isEqualTo("auctionSeq");
        }

        @Test
        void 경매_생성_중_자신이_등록한_상품이_아니면_예외가_발생한다() {
            // given
            Product product = ProductFixture.ofUser(1L, 1L);
            AuctionCreateRequest dto = new AuctionCreateRequest(
                    1L,
                    10000L,
                    100000L,
                    1000,
                    true,
                    true,
                    LocalDateTime.now().plusHours(2)
            );

            given(productRepository.findByIdWithSeller(anyLong())).willReturn(Optional.of(product));

            // when & then
            assertThatThrownBy(() -> auctionService.create(dto, 2L))
                    .isInstanceOf(ClientException.class)
                    .extracting(ERROR_CODE_ENUM_NAME)
                    .isEqualTo(ErrorCode.NOT_OWNING_PRODUCT);
        }
    }

    @Nested
    class 경매_조회 {
        @Test
        void 경매_단건_조회_는_조회수를_증가시킨다() {
            // given
            Auction auction = defaultAuction(1L, 1L, "seq");
            given(auctionRepository.findOpenOneByAuctionSeq(anyString()))
                    .willReturn(Optional.of(auction));

            // when
            auctionService.findOneAndIncreaseView("seq");

            // then
            assertThat(auction.getViews()).isEqualTo(1);
        }

        @Test
        void 경매_내역_조회_에서_진행중인_경매는_현재_가격과_종료_예정_시각을_포함한다() {
            // given
            Auction openAuction = defaultAuction(1L, 1L, "seq");
            openAuction.open();

            given(bidRepository.findJoinedAuction(any(Pageable.class), anyLong()))
                    .willReturn(new PageImpl<>(List.of(openAuction)));
            given(auctionRedisRepository.findCurrentPrice(anyString()))
                    .willReturn(1000L);

            // when
            List<AuctionLog> results = auctionService.findJoinedAuction(
                    new PageCond(1, 10), 2L
            ).getContent();

            // then
            assertThat(results).allSatisfy(r -> {
                assertThat(r.getPrice()).isEqualTo(1000L);
                assertThat(r.getExpiredTime()).isEqualTo(openAuction.getExpiresAt());
                assertThat(r.getIsWinner()).isEqualTo(false);
            });
        }

        @Test
        void 경매_내역_조회_에서_유찰된_경매는_시작_가격과_종료_시각을_포함한다() {
            // given
            Auction miscarryAuction = defaultAuction(1L, 1L, "seq");
            miscarryAuction.open();
            miscarryAuction.misCarry();

            given(bidRepository.findJoinedAuction(any(Pageable.class), anyLong()))
                    .willReturn(new PageImpl<>(List.of(miscarryAuction)));

            // when
            List<AuctionLog> results = auctionService.findJoinedAuction(
                    new PageCond(1, 10), 2L
            ).getContent();

            // then
            assertThat(results).allSatisfy(r -> {
                assertThat(r.getPrice()).isEqualTo(miscarryAuction.getStartPrice());
                assertThat(r.getExpiredTime()).isEqualTo(miscarryAuction.getExpiresAt());
                assertThat(r.getIsWinner()).isEqualTo(false);
            });
        }

        @Test
        void 경매_내역_조회_에서_완료된_경매는_낙찰_가격과_종료_시각을_포함한다() {
            // given
            LocalDateTime doneAt = LocalDateTime.now();

            Auction doneAuction = defaultAuction(1L, 1L, "seq");
            doneAuction.open();
            doneAuction.finalize(2L, 10000L, doneAt);

            given(bidRepository.findJoinedAuction(any(Pageable.class), anyLong()))
                    .willReturn(new PageImpl<>(List.of(doneAuction)));

            // when
            List<AuctionLog> results = auctionService.findJoinedAuction(
                    new PageCond(1, 10), 2L
            ).getContent();

            // then
            assertThat(results).allSatisfy(r -> {
                assertThat(r.getPrice()).isEqualTo(10000L);
                assertThat(r.getExpiredTime()).isEqualTo(doneAt);
            });
        }
    }

    @Nested
    class 경매_변경 {
        @Test
        void 경매_취소_를_공개_전_상태가_아닌데_시도하면_예외가_발생한다() {
            // given
            Auction auction = defaultAuction(1L, 1L, "seq");
            auction.open();

            given(auctionRepository.findOneBySeqAndSellerId(anyString(), anyLong()))
                    .willReturn(Optional.of(auction));

            // when & then
            assertThatThrownBy(() -> auctionService.cancel("seq", 1L))
                    .isInstanceOf(ClientException.class)
                    .extracting(ERROR_CODE_ENUM_NAME)
                    .isEqualTo(ErrorCode.CLOSE_NOT_PENDING);

        }

        @Test
        void 경매_취소_가_성공하면_취소_상태가_되고_취소_시각이_기록된다() {
            // given
            LocalDateTime canceledAt = LocalDateTime.now();
            Auction auction = defaultAuction(1L, 1L, "seq");

            given(clockHolder.now()).willReturn(canceledAt);
            given(auctionRepository.findOneBySeqAndSellerId(anyString(), anyLong()))
                    .willReturn(Optional.of(auction));
            // when
            auctionService.cancel("seq", 1L);

            // then
            assertSoftly(softly -> {
                softly.assertThat(auction.getStatus()).isEqualTo(CANCELED);
                softly.assertThat(auction.getDoneAt()).isEqualTo(canceledAt);
            });
        }

        @Test
        void 경매_공개_를_공개_전_상태가_아닌데_시도하면_예외가_발생한다() {
            // given
            Auction auction = defaultAuction(1L, 1L, "seq");
            auction.open();

            given(auctionRepository.findOneBySeqAndSellerId(anyString(), anyLong()))
                    .willReturn(Optional.of(auction));

            // when & then
            assertThatThrownBy(() -> auctionService.open("seq", 1L))
                    .isInstanceOf(ClientException.class)
                    .extracting(ERROR_CODE_ENUM_NAME)
                    .isEqualTo(ErrorCode.AUCTION_ALREADY_OPEN);
        }

        @Test
        void 경매_공개_가_성공하면_공개_상태가_된다() {
            // given
            Auction auction = defaultAuction(1L, 1L, "seq");

            given(auctionRepository.findOneBySeqAndSellerId(anyString(), anyLong()))
                    .willReturn(Optional.of(auction));

            // when
            auctionService.open("seq", 1L);

            // then
            assertThat(auction.getStatus()).isEqualTo(OPEN);
        }

        @Test
        void 경매_공개_가_성공하면_공개_이벤트를_발행한다() {
            // given
            Auction auction = defaultAuction(1L, 1L, "seq");

            given(auctionRepository.findOneBySeqAndSellerId(anyString(), anyLong()))
                    .willReturn(Optional.of(auction));

            // when
            auctionService.open("seq", 1L);

            // then
            ArgumentCaptor<AuctionOpenEvent> captor = ArgumentCaptor.forClass(AuctionOpenEvent.class);
            verify(applicationEventPublisher, times(1)).publishEvent(captor.capture());

            assertSoftly(softly -> {
                AuctionOpenEvent event = captor.getValue();
                softly.assertThat(event.getAuctionSeq()).isEqualTo("seq");
                softly.assertThat(event.getStartPrice()).isEqualTo(auction.getStartPrice());
                softly.assertThat(event.getExpiresAt()).isEqualTo(auction.getExpiresAt());
            });
        }

        @Test
        void 경매_정보_변경_이_성공적으로_이뤄진다() {
            // given
            Auction auction = defaultAuction(1L, 1L, "seq");
            LocalDateTime newExpiresAt = LocalDateTime.now();

            AuctionUpdateRequest request = new AuctionUpdateRequest(
                    500L,
                    500L,
                    500,
                    false,
                    false,
                    newExpiresAt
            );

            given(auctionRepository.findOneBySeqAndSellerId(anyString(), anyLong()))
                    .willReturn(Optional.of(auction));

            // when
            auctionService.updateInfo("seq", 1L, request);

            // then
            assertSoftly(softly -> {
                softly.assertThat(auction.getStartPrice()).isEqualTo(500L);
                softly.assertThat(auction.getBuyNowPrice()).isEqualTo(500L);
                softly.assertThat(auction.getBidUnit()).isEqualTo(500);
                softly.assertThat(auction.isBuyNowSet()).isFalse();
                softly.assertThat(auction.isAutoExtensible()).isFalse();
                softly.assertThat(auction.getExpiresAt()).isEqualTo(newExpiresAt);

            });
        }

        @Test
        void 경매_정보_변경_을_공개_전_상태가_아닌데_시도하면_예외가_발생한다() {
            // given
            Auction auction = defaultAuction(1L, 1L, "seq");
            auction.open();

            LocalDateTime newExpiresAt = LocalDateTime.now();

            AuctionUpdateRequest request = new AuctionUpdateRequest(
                    500L,
                    500L,
                    500,
                    false,
                    false,
                    newExpiresAt
            );

            given(auctionRepository.findOneBySeqAndSellerId(anyString(), anyLong()))
                    .willReturn(Optional.of(auction));

            // when & then
            assertThatThrownBy(() -> auctionService.updateInfo("seq", 1L, request))
                    .isInstanceOf(ClientException.class)
                    .extracting(ERROR_CODE_ENUM_NAME)
                    .isEqualTo(ErrorCode.AUCTION_ALREADY_OPEN);

        }

    }
}
