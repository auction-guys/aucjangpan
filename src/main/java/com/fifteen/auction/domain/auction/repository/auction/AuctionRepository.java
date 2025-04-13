package com.fifteen.auction.domain.auction.repository.auction;

import com.fifteen.auction.domain.auction.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AuctionRepository extends JpaRepository<Auction, Long>, AuctionCustomRepository {

    @Query("""
            SELECT a FROM Auction a JOIN FETCH a.product p JOIN FETCH p.seller s
            WHERE a.auctionSeq = :auctionSeq AND s.id = :sellerId 
            """)
    Optional<Auction> findOneBySeqAndSellerId(@Param("auctionSeq") String auctionSeq, @Param("sellerId") Long sellerId);

    @Query("SELECT a FROM Auction a JOIN FETCH a.product p JOIN FETCH p.seller s WHERE a.auctionSeq = :auctionSeq")
    Optional<Auction> findOpenOneBySeqWithSeller(@Param("auctionSeq") String auctionSeq);
}
