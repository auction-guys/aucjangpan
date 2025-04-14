package com.fifteen.auction.domain.tag.repository;

import com.fifteen.auction.domain.tag.entity.AuctionTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface AuctionTagRepository extends JpaRepository<AuctionTag, Long> {

    @Query("SELECT at.tag.id FROM AuctionTag at WHERE at.auction.id IN :auctionIds")
    List<Long> findTagIdsByAuctionIds(@Param("auctionIds") Set<Long> auctionIds);

    @Query("SELECT at FROM AuctionTag at JOIN FETCH at.tag WHERE at.auction.id = :auctionId")
    List<AuctionTag> findAllWithTagByAuctionId(@Param("auctionId") Long auctionId);

    @Modifying
    @Query("DELETE FROM AuctionTag at WHERE at.auction.id = :auctionId")
    void bulkDeleteByAuctionId(@Param("auctionId") Long auctionId);
}