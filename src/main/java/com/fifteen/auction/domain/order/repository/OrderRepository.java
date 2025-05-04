package com.fifteen.auction.domain.order.repository;

import com.fifteen.auction.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface OrderRepository extends JpaRepository<Order, String>, OrderRepositoryCustom {

    @Query("select o from Order o " +
            "join fetch o.auction a " +
            "join fetch a.product p " +
            "join fetch p.seller u " +
            "where o.id = :orderId")
    Optional<Order> findByOrderId(@Param("orderId") String orderId);

    @Query("select o from Order o " +
            "join fetch o.auction " +
            "where o.id = :orderId")
    Optional<Order> findByIdFetchAuction(String orderId);
}
