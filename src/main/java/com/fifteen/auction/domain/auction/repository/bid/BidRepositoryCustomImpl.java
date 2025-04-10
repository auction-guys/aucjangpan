package com.fifteen.auction.domain.auction.repository.bid;

import com.fifteen.auction.domain.auction.dto.response.BidHistoryInfo;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.fifteen.auction.domain.auction.entity.QAuction.auction;
import static com.fifteen.auction.domain.auction.entity.QBid.bid;

public class BidRepositoryCustomImpl implements BidRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public BidRepositoryCustomImpl(EntityManager entityManager) {
        queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public Page<BidHistoryInfo> findAllInProgressByAuctionSeq(Pageable pageable, String auctionSeq) {

        List<BidHistoryInfo> result = queryFactory
                .selectFrom(bid)
                .join(bid.auction, auction)
                .where(bid.auction.auctionSeq.eq(auctionSeq))
                .fetch()
                .stream()
                .map(BidHistoryInfo::forProgress)
                .toList();

        JPAQuery<Long> countQuery = queryFactory
                .select(bid.count())
                .from(bid)
                .join(bid.auction, auction)
                .where(bid.auction.auctionSeq.eq(auctionSeq));

        return PageableExecutionUtils.getPage(result, pageable, countQuery::fetchOne);
    }
}
