package com.fifteen.auction.domain.tag.repository;

import com.fifteen.auction.domain.tag.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findAllByNameInIgnoreCase(List<String> names);
    Optional<Tag> findByNameIgnoreCase(String name);

    @Query("SELECT t FROM Tag t JOIN AuctionTag at ON at.tag = t WHERE at.auction.id = :auctionId")
    List<Tag> findTagsByAuctionId(@Param("auctionId") Long auctionId);  // Auction ID로 Tag 조회
}