package com.fifteen.auction.domain.user.repository;

import com.fifteen.auction.domain.recommend.entity.RecommendGroup;
import com.fifteen.auction.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByIdAndDeletedFalse(Long id);

    Optional<User> findByEmailAndDeletedFalse(String email);

    boolean existsByEmailAndDeletedFalse(String email);

    boolean existsByNickname(String nickname);

    @Modifying
    @Query("UPDATE User u SET u.deleted = true WHERE u.email = :email")
    void softDeleteByEmail(@Param("email") String email);

    List<User> findByRecommendGroup(RecommendGroup recommendGroup);
}
