package com.fifteen.auction.domain.auction.repository;

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

public class AuctionRepositoryCustomImpl implements AuctionRepositoryCustom {

    private JPAQueryFactory queryFactory;

    public AuctionRepositoryCustomImpl(EntityManager em) {
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
}
