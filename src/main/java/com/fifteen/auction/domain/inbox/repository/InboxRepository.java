package com.fifteen.auction.domain.inbox.repository;

import com.fifteen.auction.domain.inbox.entity.Inbox;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InboxRepository extends JpaRepository<Inbox, Long>, InboxBulkRepository {

    Page<Inbox> findAllByUserId(Pageable pageable, Long userId);

    @Query("SELECT i FROM Inbox i JOIN FETCH i.user u WHERE i.id = :messageId AND i.user.id = :userId")
    Optional<Inbox> findOneByMsgIdAndUserId(@Param("messageId") Long messageId, @Param("userId") Long userId);

}
