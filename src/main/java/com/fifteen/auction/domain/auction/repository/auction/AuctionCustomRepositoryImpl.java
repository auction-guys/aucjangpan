package com.fifteen.auction.domain.auction.repository.auction;

import com.fifteen.auction.domain.auction.dto.response.AuctionListItem;
import com.fifteen.auction.domain.auction.entity.Auction;
import com.fifteen.auction.domain.auction.entity.AuctionStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;
import java.util.Optional;

import static com.fifteen.auction.domain.auction.entity.QAuction.auction;
import static com.fifteen.auction.domain.product.entity.QProduct.product;
import static com.fifteen.auction.domain.user.entity.QUser.user;

public class AuctionCustomRepositoryImpl implements AuctionCustomRepository {

    private JPAQueryFactory queryFactory;

    public AuctionCustomRepositoryImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<AuctionListItem> findAllOpenByCond(Pageable pageable) {
        List<AuctionListItem> result = queryFactory
                .selectFrom(auction)
                .join(auction.product, product).fetchJoin()
                .join(product.seller, user).fetchJoin()
                .where(statusIsOpen())
                .orderBy(auction.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()
                .stream()
                .map(AuctionListItem::fromAuction)
                .toList();

        JPAQuery<Long> countQuery = queryFactory
                .select(auction.count())
                .from(auction)
                .where(statusIsOpen());

        return PageableExecutionUtils.getPage(result, pageable, countQuery::fetchOne);
    }

    @Override
    public Optional<Auction> findOpenOneByAuctionSeq(String auctionSeq) {
        Auction result = queryFactory
                .selectFrom(auction)
                .join(auction.product, product).fetchJoin()
                .where(auctionSeqEquals(auctionSeq), statusIsOpen())
                .fetchOne();
        return Optional.ofNullable(result);
    }

    private BooleanExpression statusIsOpen() {
        return auction.status.eq(AuctionStatus.OPEN);
    }

    private BooleanExpression auctionSeqEquals(String auctionSeq) {
        return auction.auctionSeq.eq(auctionSeq);
    }

    private BooleanExpression sellerIdEquals(Long sellerId) {
        return auction.product.seller.id.eq(sellerId);
    }

    @Override
    public List<AuctionListItem> findListItemsByIds(List<Long> ids) {
        if (ids.isEmpty()) return List.of();

        List<Auction> auctions = queryFactory
                .selectFrom(auction)
                .join(auction.product, product).fetchJoin()
                .join(product.seller, user).fetchJoin()
                .where(auction.id.in(ids), auction.status.eq(AuctionStatus.OPEN))
                .fetch();

        // Redis에서의 순서 유지
        return ids.stream()
                .map(id -> auctions.stream()
                        .filter(a -> a.getId().equals(id))
                        .findFirst()
                        .map(AuctionListItem::fromAuction)
                        .orElse(null))
                .filter(a -> a != null)
                .toList();
    }

    @Override
    public Page<AuctionListItem> findAllOpenExcludingIds(List<Long> excludeIds, Pageable pageable) {
        List<Auction> result = queryFactory
                .selectFrom(auction)
                .join(auction.product, product).fetchJoin()
                .join(product.seller, user).fetchJoin()
                .where(
                        auction.status.eq(AuctionStatus.OPEN),
                        excludeIds == null || excludeIds.isEmpty() ? null : auction.id.notIn(excludeIds)
                )
                .orderBy(auction.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<AuctionListItem> items = result.stream()
                .map(AuctionListItem::fromAuction)
                .toList();

        JPAQuery<Long> countQuery = queryFactory
                .select(auction.count())
                .from(auction)
                .where(
                        auction.status.eq(AuctionStatus.OPEN),
                        excludeIds == null || excludeIds.isEmpty() ? null : auction.id.notIn(excludeIds)
                );

        return PageableExecutionUtils.getPage(items, pageable, countQuery::fetchOne);
    }


}

