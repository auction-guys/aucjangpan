package com.fifteen.auction.domain.auction.repository;

import com.fifteen.auction.domain.auction.dto.response.AuctionListItem;
import com.fifteen.auction.domain.auction.entity.Auction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface AuctionRepositoryCustom {

    Page<AuctionListItem> findAllOpenByCond(Pageable pageable);

    Optional<Auction> findOpenOneByAuctionSeq(String auctionSeq);

    Optional<Auction> findOneBySeqAndSellerId(String auctionSeq, Long sellerId);

}
