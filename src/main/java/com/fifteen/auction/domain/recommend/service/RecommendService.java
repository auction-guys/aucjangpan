package com.fifteen.auction.domain.recommend.service;

import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.domain.auction.repository.AuctionRepository;
import com.fifteen.auction.domain.auction.repository.BidRepository;
import com.fifteen.auction.domain.recommend.dto.response.RecommendationResponse;
import com.fifteen.auction.domain.recommend.entity.Recommendation;
import com.fifteen.auction.domain.recommend.entity.RecommendGroup;
import com.fifteen.auction.domain.recommend.repository.RecommendationRepository;
import com.fifteen.auction.domain.tag.repository.AuctionTagRepository;
import com.fifteen.auction.domain.tag.repository.TagRepository;
import com.fifteen.auction.domain.user.entity.User;
import com.fifteen.auction.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RecommendService {

    private final UserRepository userRepository;
    private final BidRepository bidRepository;
    private final AuctionTagRepository auctionTagRepository;
    private final RecommendationRepository recommendationRepository;
    private final AuctionRepository auctionRepository;
    private final TagRepository tagRepository;

    /**
     * 그룹 기반 추천 생성
     */
    public void generateRecommendationsForGroup(RecommendGroup group) {
        // Step 1: 그룹 유저 조회
        List<User> users = userRepository.findByRecommendGroup(group);
        if (users.isEmpty()) return;

        // Step 2: 유저 입찰 기반 경매 ID 수집
        Set<Long> auctionIds = users.stream()
                .flatMap(user -> bidRepository.findBidsByUserId(user.getId()).stream())
                .map(bid -> bid.getAuction().getId())
                .collect(Collectors.toSet());

        if (auctionIds.isEmpty()) return;

        // Step 3: 태그 ID 수집
        List<Long> tagIds = auctionTagRepository.findTagIdsByAuctionIds(auctionIds);
        if (tagIds.isEmpty()) return;

        // Step 4: 태그 빈도 계산
        Map<Long, Integer> tagFrequencyMap = new HashMap<>();
        for (Long tagId : tagIds) {
            tagFrequencyMap.merge(tagId, 1, Integer::sum);
        }

        // Step 5: 상위 10개 태그 선택
        List<Map.Entry<Long, Integer>> topTags = tagFrequencyMap.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(10)
                .toList();

        // Step 6: 이전 추천 삭제
        recommendationRepository.deleteAllByRecommendGroup(group);

        // Step 7: 태그 → 경매 매핑 & 점수 누적
        Map<Long, Integer> auctionScoreMap = new HashMap<>();
        for (Map.Entry<Long, Integer> entry : topTags) {
            Long tagId = entry.getKey();
            int tagScore = entry.getValue();

            List<Auction> auctions = auctionRepository.findOpenAuctionsByTag(tagId);
            for (Auction auction : auctions) {
                auctionScoreMap.merge(auction.getId(), tagScore, Integer::sum);
            }
        }

        // Step 8: 상위 10개 경매 추출
        List<Map.Entry<Long, Integer>> sortedAuctions = auctionScoreMap.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(10)
                .toList();

        // Step 9: 추천 저장
        Map<Long, Auction> auctionMap = auctionRepository.findAllById(
                        sortedAuctions.stream().map(Map.Entry::getKey).toList()
                )
                .stream()
                .collect(Collectors.toMap(Auction::getId, a -> a));

        int ranking = 1;
        for (Map.Entry<Long, Integer> entry : sortedAuctions) {
            Long auctionId = entry.getKey();
            int score = entry.getValue();
            Auction auction = auctionMap.get(auctionId);

            Recommendation recommendation = Recommendation.create(group, auction, score, ranking++);
            recommendationRepository.save(recommendation);
        }
    }

    /**
     * 그룹 기반 추천 조회
     */
    public List<RecommendationResponse> getRecommendationsForGroup(RecommendGroup group) {
        return recommendationRepository.findAllByRecommendGroupOrderByRankingAsc(group).stream()
                .map(RecommendationResponse::from)
                .toList();
    }
}