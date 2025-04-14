package com.fifteen.auction.domain.tag.service;

import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.domain.tag.entity.AuctionTag;
import com.fifteen.auction.domain.tag.entity.Tag;
import com.fifteen.auction.domain.tag.repository.AuctionTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuctionTagService {

    private final AuctionTagRepository auctionTagRepository;

    public void registerTagsToAuction(Auction auction, List<Tag> tags) {
        List<AuctionTag> auctionTags = tags.stream()
                .map(tag -> AuctionTag.create(auction, tag))
                .collect(Collectors.toList());

        auctionTagRepository.saveAll(auctionTags);
    }

    @Transactional
    public void updateAuctionTags(Auction auction, List<Tag> newTags) {
        auctionTagRepository.bulkDeleteByAuctionId(auction.getId());
        registerTagsToAuction(auction, newTags);
    }
}
