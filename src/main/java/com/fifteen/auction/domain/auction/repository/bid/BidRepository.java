package com.fifteen.auction.domain.auction.repository.bid;

import com.fifteen.auction.domain.auction.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface BidRepository extends JpaRepository<Bid, Long>, BidCustomRepository {

    // 특정 경매에 대한 입찰 정보를 조회
    @Query("SELECT DISTINCT b.auction.id FROM Bid b WHERE b.bidderId IN :userIds")
    Set<Long> findAuctionIdsByUserIds(@Param("userIds") List<Long> userIds);
}
