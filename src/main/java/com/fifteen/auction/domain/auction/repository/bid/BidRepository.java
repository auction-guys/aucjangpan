package com.fifteen.auction.domain.auction.repository.bid;

import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.domain.auction.entity.Bid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface BidRepository extends JpaRepository<Bid, Long>, BidCustomRepository {

    // 특정 경매에 대한 입찰 정보를 조회
    @Query("SELECT DISTINCT b.auction.id FROM Bid b WHERE b.bidderId IN :userIds")
    Set<Long> findAuctionIdsByUserIds(@Param("userIds") List<Long> userIds);

    @Query("SELECT DISTINCT a FROM Bid b" +
            " JOIN b.auction a JOIN FETCH a.product p JOIN FETCH p.seller" +
            " WHERE b.bidderId = :userId")
    Page<Auction> findJoinedAuction(Pageable pageable, @Param("userId") Long userId);
}
