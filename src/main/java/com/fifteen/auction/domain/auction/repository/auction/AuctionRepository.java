package com.fifteen.auction.domain.auction.repository.auction;

import com.fifteen.auction.domain.auction.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AuctionRepository extends JpaRepository<Auction, Long>, AuctionCustomRepository {

    @Query("""
            SELECT a FROM Auction a JOIN FETCH a.product p JOIN FETCH p.seller s
            WHERE a.auctionSeq = :auctionSeq AND s.id = :sellerId 
            """)
    Optional<Auction> findOneBySeqAndSellerId(@Param("auctionSeq") String auctionSeq, @Param("sellerId") Long sellerId);

    @Query("SELECT a FROM Auction a JOIN FETCH a.product p JOIN FETCH p.seller s WHERE a.auctionSeq = :auctionSeq")
    Optional<Auction> findOpenOneBySeqWithSeller(@Param("auctionSeq") String auctionSeq);
           
    // Auction과 Tag를 연결하는 AuctionTag를 통해 경매와 태그를 찾음
    @Query("SELECT DISTINCT a FROM Auction a " +
            "JOIN a.tags at " +  // auctionTags 필드 참조
            "WHERE at.tag.id = :tagId AND a.status = 'OPEN'")
    List<Auction> findOpenAuctionsByTag(@Param("tagId") Long tagId);

}
