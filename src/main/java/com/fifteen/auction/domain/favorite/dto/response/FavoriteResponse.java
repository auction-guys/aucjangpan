package com.fifteen.auction.domain.favorite.dto.response;

import com.fifteen.auction.domain.favorite.entity.Favorite;
import lombok.Getter;

@Getter
public class FavoriteResponse {

    private final Long auctionId;
    private final String productName;
    private final String nickname;
    private final int reliability;

    private FavoriteResponse(Long auctionId,String productName, String nickname, int reliability) {
        this.auctionId = auctionId;
        this.productName = productName;
        this.nickname = nickname;
        this.reliability = reliability;
    }

    public static FavoriteResponse from(Favorite favorite) {
        return new FavoriteResponse(
                favorite.getAuction().getId(),
                favorite.getAuction().getProduct().getName(),
                favorite.getUser().getNickname(),
                0
        );
    }
}