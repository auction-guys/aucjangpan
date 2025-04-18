package com.fifteen.auction.domain.recommend.service;

import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.domain.auction.repository.auction.AuctionRepository;
import com.fifteen.auction.domain.auction.repository.bid.BidRepository;
import com.fifteen.auction.domain.recommend.dto.response.RecommendationResponse;
import com.fifteen.auction.domain.recommend.entity.RecommendGroup;
import com.fifteen.auction.domain.recommend.repository.RecommendGroupRepository;
import com.fifteen.auction.domain.tag.repository.AuctionTagRepository;
import com.fifteen.auction.domain.user.entity.User;
import com.fifteen.auction.domain.user.repository.UserRepository;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import com.fifteen.auction.infra.redis.repository.RecommendRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RecommendService {

    private final UserRepository userRepository;
    private final BidRepository bidRepository;
    private final AuctionTagRepository auctionTagRepository;
    private final RecommendGroupRepository recommendGroupRepository;
    private final RecommendRedisRepository recommendRedisRepository;
    private final AuctionRepository auctionRepository;

    public void generateRecommendationsForGroup(RecommendGroup group) {
        List<User> users = userRepository.findByRecommendGroup(group);
        if (users.isEmpty()) return;

        List<Long> userIds = users.stream().map(User::getId).toList();
        Set<Long> auctionIds = bidRepository.findAuctionIdsByUserIds(userIds);
        if (auctionIds.isEmpty()) return;

        List<Long> tagIds = auctionTagRepository.findTagIdsByAuctionIds(auctionIds);
        if (tagIds.isEmpty()) return;

        Map<Long, Integer> tagFrequency = new HashMap<>();
        for (Long tagId : tagIds) {
            tagFrequency.merge(tagId, 1, Integer::sum);
        }

        List<Long> topTagIds = tagFrequency.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(10)
                .map(Map.Entry::getKey)
                .toList();

        List<Auction> auctions = auctionRepository.findOpenAuctionsByTagIds(topTagIds);
        Map<Long, Integer> scoreMap = new HashMap<>();

        for (Auction auction : auctions) {
            Set<Long> auctionTagIds = auction.getTagIds(); // List → Set 변경 권장
            for (Long tagId : topTagIds) {
                if (auctionTagIds.contains(tagId)) {
                    scoreMap.merge(auction.getId(), tagFrequency.get(tagId), Integer::sum);
                }
            }
        }

        List<Map.Entry<Long, Integer>> sorted = scoreMap.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(10)
                .toList();

        List<RecommendRedisRepository.AuctionScore> scores = sorted.stream()
                .map(e -> new RecommendRedisRepository.AuctionScore(e.getKey(), e.getValue()))
                .toList();

        recommendRedisRepository.saveRecommendations(group.getId(), scores);
    }

    @Transactional(readOnly = true)
    public List<RecommendationResponse> getRecommendationsForGroup(RecommendGroup group) {
        Set<ZSetOperations.TypedTuple<String>> results = recommendRedisRepository.getTopRecommendations(group.getId(), 10);
        if (results.isEmpty()) return List.of();

        List<Long> auctionIds = results.stream().map(t -> Long.valueOf(t.getValue())).toList();
        List<Auction> auctions = auctionRepository.findAllById(auctionIds);
        Map<Long, Auction> auctionMap = auctions.stream().collect(Collectors.toMap(Auction::getId, a -> a));

        List<RecommendationResponse> response = new ArrayList<>();
        int ranking = 1;
        for (ZSetOperations.TypedTuple<String> tuple : results) {
            Long id = Long.valueOf(tuple.getValue());
            Auction auction = auctionMap.get(id);
            if (auction != null) {
                response.add(RecommendationResponse.of(auction, tuple.getScore().intValue(), ranking++));
            }
        }
        return response;
    }

    @Transactional(readOnly = true)
    public RecommendGroup findGroup(Long groupId) {
        return recommendGroupRepository.findById(groupId)
                .orElseThrow(() -> new ClientException(ErrorCode.RECOMMEND_NOT_FOUND));
    }
}