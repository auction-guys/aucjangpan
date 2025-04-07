package com.fifteen.auction.domain.auction.repository;

import com.fifteen.auction.domain.auction.dto.response.AuctionListItem;
import com.fifteen.auction.domain.auction.entity.Auction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AuctionRepositoryCustom {

    Page<AuctionListItem> findAllByCond(Pageable pageable);

    Optional<Auction> findOpenOneByAuctionSeq(String auctionSeq);

    Optional<Auction> findOpenOneBySeqAndSellerId(@Param("auctionSeq") String auctionSeq,
                                                  @Param("sellerId") Long sellerId);
}
