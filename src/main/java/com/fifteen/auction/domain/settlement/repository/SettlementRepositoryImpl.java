package com.fifteen.auction.domain.settlement.repository;

import com.fifteen.auction.domain.settlement.dto.response.SettlementResponse;
import com.fifteen.auction.domain.settlement.entity.Settlement;
import com.fifteen.auction.domain.settlement.enums.SettlementStatus;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.fifteen.auction.domain.auction.entity.QAuction.auction;
import static com.fifteen.auction.domain.product.entity.QProduct.product;
import static com.fifteen.auction.domain.order.entity.QOrder.order;
import static com.fifteen.auction.domain.settlement.entity.QSettlement.settlement;
import static com.fifteen.auction.domain.user.entity.QUser.user;

public class SettlementRepositoryImpl implements SettlementRepositoryCustom {

    private JPAQueryFactory queryFactory;

    public SettlementRepositoryImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<Settlement> findAllByStatus(SettlementStatus settlementStatus) {

        List<Settlement> settlements = queryFactory
                .selectFrom(settlement)
                .leftJoin(settlement.order, order).fetchJoin()
                .leftJoin(order.auction, auction).fetchJoin()
                .leftJoin(auction.product, product).fetchJoin()
                .leftJoin(product.seller, user).fetchJoin()
                .where(settlement.status.eq(settlementStatus))
                .fetch();

        return settlements;
    }

    @Override
    public Page<SettlementResponse> findBySellerId(Long currentUserId, Pageable pageable) {

        List<SettlementResponse> query = queryFactory
                .select(Projections.constructor(
                        SettlementResponse.class,
                        settlement.id.stringValue(),
                        user.id.stringValue(),
                        order.id,
                        auction.winPrice.stringValue(),
                        settlement.charge.stringValue(),
                        settlement.settlementAmount.stringValue(),
                        settlement.status.stringValue(),
                        settlement.settledAt.stringValue(),
                        settlement.createdAt.stringValue(),
                        user.accountNumber.stringValue()
                ))
                .from(settlement)
                .join(settlement.order, order)
                .join(order.auction, auction)
                .join(auction.product.seller, user)
                .where(user.id.eq(currentUserId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(settlement.count())
                .from(settlement)
                .join(settlement.order, order)
                .join(order.auction, auction)
                .join(auction.product, product)
                .join(product.seller, user)
                .where(user.id.eq(currentUserId));

        return PageableExecutionUtils.getPage(query, pageable, countQuery::fetchOne);
    }
}
