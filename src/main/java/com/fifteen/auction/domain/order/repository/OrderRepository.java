package com.fifteen.auction.domain.order.repository;

import com.fifteen.auction.domain.order.entity.Order;
import com.fifteen.auction.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface OrderRepository extends JpaRepository<Order, Long>, OrderRepositoryCustom {

    @Query("select o from Order o " +
            "join fetch o.auction a " +
            "join fetch a.product p " +
            "join fetch p.seller u " +
            "where o.id = :orderId")
    Optional<Order> findByOrderId(@Param("orderId") Long orderId);
}
