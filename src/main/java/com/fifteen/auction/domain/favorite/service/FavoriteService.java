package com.fifteen.auction.domain.favorite.service;

import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.domain.auction.repository.auction.AuctionRepository;
import com.fifteen.auction.domain.favorite.dto.response.FavoriteResponse;
import com.fifteen.auction.domain.favorite.entity.Favorite;
import com.fifteen.auction.domain.favorite.repository.FavoriteRepository;
import com.fifteen.auction.domain.user.entity.User;
import com.fifteen.auction.domain.user.repository.UserRepository;
import com.fifteen.auction.global.dto.PageCond;
import com.fifteen.auction.global.dto.PageInfo;
import com.fifteen.auction.global.dto.Response;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    // 찜 하기
    public void like(Long userId, Long auctionId) {
        if (favoriteRepository.existsByUserIdAndAuctionId(userId, auctionId)) {
            throw new ClientException(ErrorCode.DUPLICATE_FAVORITE);
        }

        User user = findUser(userId);
        Auction auction = findAuction(auctionId);

        favoriteRepository.save(Favorite.create(user, auction));
    }

    // 찜 하기 취소
    public void unlike(Long userId, Long auctionId) {
        Favorite favorite = favoriteRepository.findByUserIdAndAuctionId(userId, auctionId)
                .orElseThrow(() -> new ClientException(ErrorCode.FAVORITE_NOT_FOUND));

        favoriteRepository.delete(favorite);
    }

    // 내 찜 목록 조회 (페이징 포함)
    @Transactional(readOnly = true)
    public Response<List<FavoriteResponse>> findMyFavorites(Long userId, PageCond pageCond) {
        Pageable pageable = PageRequest.of(pageCond.getPageNum() - 1, pageCond.getPageSize());

        Page<Favorite> page = favoriteRepository.findAllWithAuctionByUserId(userId, pageable);

        List<FavoriteResponse> content = page.getContent().stream()
                .map(FavoriteResponse::from)
                .toList();

        PageInfo pageInfo = PageInfo.builder()
                .pageNum(pageCond.getPageNum())
                .pageSize(pageCond.getPageSize())
                .totalElement(page.getTotalElements())
                .totalPage(page.getTotalPages())
                .build();

        return Response.of(content, pageInfo);
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