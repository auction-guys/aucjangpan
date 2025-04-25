package com.fifteen.auction.domain.favorite.repository;

import com.fifteen.auction.domain.favorite.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserIdAndAuctionId(Long userId, Long auctionId);

    boolean existsByUserIdAndAuctionId(Long userId, Long auctionId);

    void deleteByUserIdAndAuctionId(Long userId, Long auctionId);

    @Query("SELECT f FROM Favorite f JOIN FETCH f.auction WHERE f.user.id = :userId")
    Page<Favorite> findAllWithAuctionByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.auction.id = :auctionId")
    long countByAuctionId(@Param("auctionId") Long auctionId);

    @Query("SELECT f.auction.id FROM Favorite f WHERE f.user.id IN :userIds")
    Set<Long> findAuctionIdsByUserIds(@Param("userIds") List<Long> userIds);
}