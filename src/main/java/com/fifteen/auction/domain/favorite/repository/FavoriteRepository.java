package com.fifteen.auction.domain.favorite.repository;

import com.fifteen.auction.domain.favorite.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserIdAndAuctionId(Long userId, Long auctionId);

    boolean existsByUserIdAndAuctionId(Long userId, Long auctionId);

    List<Favorite> findAllByUserId(Long userId);

    void deleteByUserIdAndAuctionId(Long userId, Long auctionId);

    @Query("SELECT f FROM Favorite f JOIN FETCH f.user JOIN FETCH f.auction WHERE f.user.id = :userId")
    List<Favorite> findAllWithUserAndAuctionByUserId(@Param("userId") Long userId);

}