package com.fifteen.auction.domain.chat.repository.room;

import com.fifteen.auction.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>, ChatRoomCustomRepository{

}
