package com.fifteen.auction.domain.auction.repository.bid;

import com.fifteen.auction.domain.auction.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BidRepository extends JpaRepository<Bid, Long> {

    // 특정 경매에 대한 입찰 정보를 조회
    @Query("SELECT b FROM Bid b WHERE b.bidderId = :userId")
    List<Bid> findBidsByUserId(@Param("userId") Long userId);
}