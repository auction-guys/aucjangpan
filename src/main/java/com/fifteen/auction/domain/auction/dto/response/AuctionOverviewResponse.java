package com.fifteen.auction.domain.auction.dto.response;

import com.fifteen.auction.global.dto.PageInfo;
import lombok.Getter;

import java.util.List;

@Getter
public class AuctionOverviewResponse {
    private final List<AuctionListItem> recommended;
    private final List<AuctionListItem> content;
    private final PageInfo pageInfo;

    public AuctionOverviewResponse(List<AuctionListItem> recommended, List<AuctionListItem> content, PageInfo pageInfo) {
        this.recommended = recommended;
        this.content = content;
        this.pageInfo = pageInfo;
    }
}
