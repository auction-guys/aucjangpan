package com.fifteen.auction.domain.favorite.service;

import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.domain.auction.repository.auction.AuctionRepository;
import com.fifteen.auction.domain.favorite.entity.Favorite;
import com.fifteen.auction.domain.favorite.repository.FavoriteRepository;
import com.fifteen.auction.domain.user.entity.User;
import com.fifteen.auction.domain.user.repository.UserRepository;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final AuctionRepository auctionRepository;

    // 찜 토글 기능 - 찜이 있으면 제거, 없으면 추가
    public void toggleFavorite(Long userId, Long auctionId) {
        Optional<Favorite> existing = favoriteRepository.findByUserIdAndAuctionId(userId, auctionId);

        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
        } else {

            User user = findUser(userId);
            Auction auction = findAuction(auctionId);

            favoriteRepository.save(Favorite.create(user, auction));
        }
    }

    public List<Favorite> findMyFavorites(Long userId) {
        return favoriteRepository.findAllWithUserAndAuctionByUserId(userId);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ClientException(ErrorCode.USER_NOT_FOUND));
    }


    private Auction findAuction(Long auctionId) {

        return auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ClientException(ErrorCode.AUCTION_NOT_FOUND));
    }
}