package com.fifteen.auction.fixtures;

import com.fifteen.auction.domain.auction.dto.request.AuctionCreateRequest;
import com.fifteen.auction.domain.auction.entity.Auction;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuctionFixture {
    public static Auction fromCreateDto(AuctionCreateRequest req, Long ownerId, String auctionSeq) {
        return new Auction(
                ProductFixture.ofUser(req.getProductId(), ownerId),
                auctionSeq,
                req.getStartPrice(),
                req.getBuyNowPrice(),
                req.getBidUnit(),
                req.getIsBuyNowSet(),
                req.getIsAutoExtensible(),
                req.getExpiresAt()
        );
    }

    public static Auction defaultAuction(Long productId, Long ownerId, String auctionSeq) {
        Auction auction = new Auction(
                ProductFixture.ofUser(productId, ownerId),
                auctionSeq,
                1000L,
                10000L,
                1000,
                true,
                true,
                LocalDateTime.now().plusHours(2)
        );
        return auction;
    }
}
