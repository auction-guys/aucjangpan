package com.fifteen.auction.domain.order.repository;

import com.fifteen.auction.domain.order.dto.response.OrderResponse;
import com.fifteen.auction.domain.order.dto.response.OrdersResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;
import java.util.Optional;

import static com.fifteen.auction.domain.auction.entity.QAuction.auction;
import static com.fifteen.auction.domain.order.entity.QOrder.order;
import static com.fifteen.auction.domain.product.entity.QProduct.product;
import static com.fifteen.auction.domain.user.entity.QUser.user;

public class OrderRepositoryImpl implements OrderRepositoryCustom{

    private JPAQueryFactory queryFactory;

    public OrderRepositoryImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<OrdersResponse> findAllByUserId(Long currentUserId, Pageable pageable) {


        List<OrdersResponse> query = queryFactory
                .select(Projections.constructor(
                        OrdersResponse.class,
                        order.id,
                        product.name,
                        auction.winPrice.stringValue(),
                        order.status.stringValue(),
                        order.createdAt.stringValue()
                ))
                .from(order)
                .join(order.user, user)
                .join(order.auction, auction)
                .join(auction.product, product)
                .where(user.id.eq(currentUserId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(order.count())
                .from(order)
                .where(order.user.id.eq(currentUserId));

        return PageableExecutionUtils.getPage(query, pageable, countQuery::fetchOne);
    }

    @Override
    public Optional<OrderResponse> findByOrderIdAndUserId(String orderId, Long currentUserId) {

        OrderResponse query = queryFactory
                .select(Projections.constructor(
                        OrderResponse.class,
                        user.name,
                        order.id,
                        user.address,
                        product.name,
                        auction.winPrice.stringValue(),
                        order.status.stringValue(),
                        order.createdAt
                ))
                .from(order)
                .join(order.auction, auction)
                .join(auction.product, product)
                .join(order.user, user)
                .where(user.id.eq(currentUserId).and(order.id.eq(orderId)))
                .fetchOne();

        return Optional.ofNullable(query);
    }
}
